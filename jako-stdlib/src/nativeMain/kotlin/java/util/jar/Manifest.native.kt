package java.util.jar

import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Cloneable

actual open class Manifest : Cloneable {
    private val attr = Attributes()
    private val entryMap: MutableMap<String, Attributes> = LinkedHashMap()

    actual constructor()

    actual constructor(isStream: InputStream) {
        read(isStream)
    }

    actual constructor(man: Manifest) {
        attr.putAll(man.attr)
        for ((k, v) in man.entryMap) {
            entryMap[k] = Attributes(v)
        }
    }

    actual open fun getMainAttributes(): Attributes = attr
    actual open val mainAttributes: Attributes get() = getMainAttributes()

    actual open fun getEntries(): MutableMap<String, Attributes> = entryMap
    actual open val entries: MutableMap<String, Attributes> get() = getEntries()

    actual open fun getAttributes(name: String): Attributes? = entryMap[name]

    actual open fun clear() {
        attr.clear()
        entryMap.clear()
    }

    actual open fun write(out: OutputStream) {
        val dos = BufferedOutputStream(out)
        val verName = when {
            attr.getValue(Attributes.Name.MANIFEST_VERSION) != null -> Attributes.Name.MANIFEST_VERSION
            attr.getValue(Attributes.Name.SIGNATURE_VERSION) != null -> Attributes.Name.SIGNATURE_VERSION
            else -> null
        }
        if (verName != null) {
            writeHeaderLine(dos, verName.toString(), attr.getValue(verName)!!)
        }
        for ((k, v) in attr.entries) {
            if (k != verName) {
                writeHeaderLine(dos, k.toString(), v.toString())
            }
        }
        dos.write("\r\n".encodeToByteArray())
        for ((sectionName, sectionAttrs) in entryMap) {
            writeHeaderLine(dos, "Name", sectionName)
            for ((k, v) in sectionAttrs.entries) {
                writeHeaderLine(dos, k.toString(), v.toString())
            }
            dos.write("\r\n".encodeToByteArray())
        }
        dos.flush()
    }

    private fun writeHeaderLine(out: OutputStream, name: String, value: String) {
        val raw = "$name: $value".encodeToByteArray()
        if (raw.size <= 70) {
            out.write(raw)
            out.write("\r\n".encodeToByteArray())
            return
        }
        // First line up to 70 UTF-8 bytes without splitting a multi-byte UTF-8 sequence
        var offset = 0
        var first = true
        while (offset < raw.size) {
            val maxChunk = if (first) 70 else 69
            var end = minOf(raw.size, offset + maxChunk)
            while (end < raw.size && (raw[end].toInt() and 0xC0) == 0x80) {
                end--
            }
            if (!first) {
                out.write(' '.code)
            }
            out.write(raw, offset, end - offset)
            out.write("\r\n".encodeToByteArray())
            offset = end
            first = false
        }
    }

    actual open fun read(isStream: InputStream) {
        val rawBytes = readAllStreamBytes(isStream)
        if (rawBytes.isEmpty()) return
        val rawLines = splitManifestLines(rawBytes)
        // Unfold continuation lines starting with a single space ' '
        val logicalLines = mutableListOf<String>()
        var currentLineBytes: ByteArrayOutputStream? = null
        for (lineBytes in rawLines) {
            if (lineBytes.isEmpty()) {
                if (currentLineBytes != null) {
                    logicalLines.add(currentLineBytes.toByteArray().decodeToString())
                    currentLineBytes = null
                }
                logicalLines.add("")
            } else if (lineBytes[0] == ' '.code.toByte()) {
                if (currentLineBytes == null) {
                    throw IOException("Invalid manifest continuation line")
                }
                currentLineBytes.write(lineBytes, 1, lineBytes.size - 1)
            } else {
                if (currentLineBytes != null) {
                    logicalLines.add(currentLineBytes.toByteArray().decodeToString())
                }
                currentLineBytes = ByteArrayOutputStream()
                currentLineBytes.write(lineBytes, 0, lineBytes.size)
            }
        }
        if (currentLineBytes != null) {
            logicalLines.add(currentLineBytes.toByteArray().decodeToString())
        }

        var idx = 0
        // Read main attributes until first blank line
        while (idx < logicalLines.size) {
            val line = logicalLines[idx++]
            if (line.isEmpty()) break
            val (k, v) = parseHeader(line)
            attr.putValue(k, v)
        }
        // Read individual sections
        while (idx < logicalLines.size) {
            while (idx < logicalLines.size && logicalLines[idx].isEmpty()) {
                idx++
            }
            if (idx >= logicalLines.size) break
            val firstLine = logicalLines[idx++]
            val (firstKey, sectionName) = parseHeader(firstLine)
            if (!firstKey.equals("Name", ignoreCase = true)) {
                throw IOException("Manifest section must start with Name header: $firstLine")
            }
            val secAttrs = entryMap.getOrPut(sectionName) { Attributes() }
            while (idx < logicalLines.size) {
                val line = logicalLines[idx++]
                if (line.isEmpty()) break
                val (k, v) = parseHeader(line)
                secAttrs.putValue(k, v)
            }
        }
    }

    private fun parseHeader(line: String): Pair<String, String> {
        val colon = line.indexOf(':')
        if (colon <= 0) throw IOException("Invalid manifest header: $line")
        val key = line.substring(0, colon)
        var valStart = colon + 1
        if (valStart < line.length && line[valStart] == ' ') {
            valStart++
        }
        val value = line.substring(valStart)
        return key to value
    }

    private fun splitManifestLines(bytes: ByteArray): List<ByteArray> {
        val lines = mutableListOf<ByteArray>()
        var start = 0
        var i = 0
        while (i < bytes.size) {
            val b = bytes[i].toInt() and 0xFF
            if (b == '\r'.code || b == '\n'.code) {
                lines.add(bytes.copyOfRange(start, i))
                if (b == '\r'.code && i + 1 < bytes.size && bytes[i + 1] == '\n'.code.toByte()) {
                    i++
                }
                start = i + 1
            }
            i++
        }
        if (start < bytes.size) {
            lines.add(bytes.copyOfRange(start, bytes.size))
        }
        return lines
    }

    actual open fun clone(): Any = Manifest(this)

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Manifest) return false
        return attr == other.attr && entryMap == other.entryMap
    }

    actual override fun hashCode(): Int = attr.hashCode() + entryMap.hashCode()
}

internal fun readAllStreamBytes(ins: InputStream): ByteArray {
    val bos = ByteArrayOutputStream()
    val buf = ByteArray(1024)
    while (true) {
        val n = ins.read(buf, 0, buf.size)
        if (n < 0) break
        bos.write(buf, 0, n)
    }
    return bos.toByteArray()
}
