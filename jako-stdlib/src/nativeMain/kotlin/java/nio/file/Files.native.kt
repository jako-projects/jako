package java.nio.file

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.nativeReadBasicAttributes

actual open class FileSystemException actual constructor(
    actual val file: String?,
    other: String?,
    actual val reason: String?
) : IOException(buildMessage(file, other, reason)) {
    actual constructor(file: String?) : this(file, null, null)
    actual val otherFile: String? = other

    companion object {
        private fun buildMessage(file: String?, other: String?, reason: String?): String {
            val sb = StringBuilder()
            if (file != null) sb.append(file)
            if (other != null) sb.append(" -> ").append(other)
            if (reason != null) {
                if (sb.isNotEmpty()) sb.append(": ")
                sb.append(reason)
            }
            return sb.toString()
        }
    }
}

actual open class NoSuchFileException actual constructor(
    file: String?,
    other: String?,
    reason: String?
) : FileSystemException(file, other, reason) {
    actual constructor(file: String?) : this(file, null, null)
}

actual open class FileAlreadyExistsException actual constructor(
    file: String?,
    other: String?,
    reason: String?
) : FileSystemException(file, other, reason) {
    actual constructor(file: String?) : this(file, null, null)
}

actual class Files {
    actual companion object {
        actual fun exists(path: Path, vararg options: LinkOption): Boolean = path.toFile().exists()

        actual fun notExists(path: Path, vararg options: LinkOption): Boolean = !exists(path, *options)

        actual fun isDirectory(path: Path, vararg options: LinkOption): Boolean = path.toFile().isDirectory()

        actual fun isRegularFile(path: Path, vararg options: LinkOption): Boolean = path.toFile().isFile()

        actual fun isReadable(path: Path): Boolean = path.toFile().exists()

        actual fun isWritable(path: Path): Boolean = path.toFile().exists()

        actual fun isExecutable(path: Path): Boolean = path.toFile().exists()

        actual fun size(path: Path): Long {
            val file = path.toFile()
            if (!file.exists()) throw NoSuchFileException(path.toString())
            return file.length()
        }

        actual fun createFile(path: Path): Path {
            val file = path.toFile()
            if (file.exists()) throw FileAlreadyExistsException(path.toString())
            val fos = FileOutputStream(file)
            fos.close()
            return path
        }

        actual fun createDirectory(path: Path): Path {
            val file = path.toFile()
            if (file.exists()) throw FileAlreadyExistsException(path.toString())
            if (!file.mkdir()) throw IOException("Failed to create directory $path")
            return path
        }

        actual fun createDirectories(path: Path): Path {
            val file = path.toFile()
            file.mkdirs()
            return path
        }

        actual fun delete(path: Path) {
            val file = path.toFile()
            if (!file.exists()) throw NoSuchFileException(path.toString())
            if (!file.delete()) throw IOException("Failed to delete $path")
        }

        actual fun deleteIfExists(path: Path): Boolean {
            val file = path.toFile()
            return if (file.exists()) file.delete() else false
        }

        actual fun copy(source: Path, target: Path, vararg options: CopyOption): Path {
            val inStream = newInputStream(source)
            try {
                copy(inStream, target, *options)
            } finally {
                inStream.close()
            }
            return target
        }

        actual fun copy(inStream: InputStream, target: Path, vararg options: CopyOption): Long {
            val replace = options.contains(StandardCopyOption.REPLACE_EXISTING)
            val file = target.toFile()
            if (file.exists() && !replace) {
                throw FileAlreadyExistsException(target.toString())
            }
            val out = FileOutputStream(file)
            var total = 0L
            try {
                val buf = ByteArray(8192)
                var r = inStream.read(buf)
                while (r != -1) {
                    out.write(buf, 0, r)
                    total += r
                    r = inStream.read(buf)
                }
            } finally {
                out.close()
            }
            return total
        }

        actual fun copy(source: Path, out: OutputStream): Long {
            val inStream = newInputStream(source)
            var total = 0L
            try {
                val buf = ByteArray(8192)
                var r = inStream.read(buf)
                while (r != -1) {
                    out.write(buf, 0, r)
                    total += r
                    r = inStream.read(buf)
                }
            } finally {
                inStream.close()
            }
            return total
        }

        actual fun move(source: Path, target: Path, vararg options: CopyOption): Path {
            copy(source, target, *options)
            delete(source)
            return target
        }

        actual fun newInputStream(path: Path, vararg options: OpenOption): InputStream {
            val file = path.toFile()
            if (!file.exists()) throw NoSuchFileException(path.toString())
            return FileInputStream(file)
        }

        actual fun newOutputStream(path: Path, vararg options: OpenOption): OutputStream {
            val file = path.toFile()
            val append = options.contains(StandardOpenOption.APPEND)
            val createNew = options.contains(StandardOpenOption.CREATE_NEW)
            if (createNew && file.exists()) {
                throw FileAlreadyExistsException(path.toString())
            }
            return FileOutputStream(file, append)
        }

        actual fun newBufferedReader(path: Path, cs: Charset): BufferedReader {
            return BufferedReader(InputStreamReader(newInputStream(path), cs))
        }

        actual fun newBufferedReader(path: Path): BufferedReader {
            return newBufferedReader(path, StandardCharsets.UTF_8)
        }

        actual fun newBufferedWriter(path: Path, cs: Charset, vararg options: OpenOption): BufferedWriter {
            return BufferedWriter(OutputStreamWriter(newOutputStream(path, *options), cs))
        }

        actual fun newBufferedWriter(path: Path, vararg options: OpenOption): BufferedWriter {
            return newBufferedWriter(path, StandardCharsets.UTF_8, *options)
        }

        actual fun readAllBytes(path: Path): ByteArray {
            val inStream = newInputStream(path)
            val baos = ByteArrayOutputStream()
            try {
                val buf = ByteArray(8192)
                var r = inStream.read(buf)
                while (r != -1) {
                    baos.write(buf, 0, r)
                    r = inStream.read(buf)
                }
            } finally {
                inStream.close()
            }
            return baos.toByteArray()
        }

        actual fun readAllLines(path: Path, cs: Charset): List<String> {
            val reader = newBufferedReader(path, cs)
            val list = mutableListOf<String>()
            try {
                var line = reader.readLine()
                while (line != null) {
                    list.add(line)
                    line = reader.readLine()
                }
            } finally {
                reader.close()
            }
            return list
        }

        actual fun readAllLines(path: Path): List<String> = readAllLines(path, StandardCharsets.UTF_8)

        actual fun write(path: Path, bytes: ByteArray, vararg options: OpenOption): Path {
            val out = newOutputStream(path, *options)
            try {
                out.write(bytes)
            } finally {
                out.close()
            }
            return path
        }

        actual fun write(path: Path, lines: Iterable<CharSequence>, cs: Charset, vararg options: OpenOption): Path {
            val writer = newBufferedWriter(path, cs, *options)
            try {
                for (l in lines) {
                    writer.write(l.toString())
                    writer.newLine()
                }
            } finally {
                writer.close()
            }
            return path
        }

        actual fun write(path: Path, lines: Iterable<CharSequence>, vararg options: OpenOption): Path {
            return write(path, lines, StandardCharsets.UTF_8, *options)
        }

        actual fun walkFileTree(start: Path, visitor: FileVisitor<in Path>): Path {
            return walkFileTree(start, emptySet(), Int.MAX_VALUE, visitor)
        }

        actual fun walkFileTree(
            start: Path,
            options: Set<FileVisitOption>,
            maxDepth: Int,
            visitor: FileVisitor<in Path>
        ): Path {
            return walkFileTreeImpl(start, maxDepth, visitor)
        }

        actual fun readAttributes(
            path: Path,
            attributes: String,
            vararg options: LinkOption
        ): MutableMap<String, Any> {
            val attrs = nativeReadBasicAttributes(path)
            val requested = parseBasicAttributeNames(attributes)
            val result = linkedMapOf<String, Any>()
            for (name in requested) {
                val value: Any = when (name) {
                    "size" -> attrs.size()
                    "lastModifiedTime" -> attrs.lastModifiedTime()
                    "lastAccessTime" -> attrs.lastAccessTime()
                    "creationTime" -> attrs.creationTime()
                    "isDirectory" -> attrs.isDirectory()
                    "isRegularFile" -> attrs.isRegularFile()
                    "isSymbolicLink" -> attrs.isSymbolicLink()
                    "isOther" -> attrs.isOther()
                    "fileKey" -> attrs.fileKey() ?: path.toString()
                    else -> throw IllegalArgumentException("Unknown attribute: $name")
                }
                result[name] = value
            }
            return result
        }

        actual fun getLastModifiedTime(path: Path, vararg options: LinkOption): FileTime {
            return nativeReadBasicAttributes(path).lastModifiedTime()
        }

        actual fun isSymbolicLink(path: Path): Boolean {
            return if (exists(path)) nativeReadBasicAttributes(path).isSymbolicLink() else false
        }
    }
}

private val ALL_BASIC_ATTRIBUTES = listOf(
    "lastModifiedTime",
    "lastAccessTime",
    "creationTime",
    "isRegularFile",
    "isDirectory",
    "isSymbolicLink",
    "isOther",
    "size",
    "fileKey"
)

private fun parseBasicAttributeNames(attributes: String): List<String> {
    val colon = attributes.indexOf(':')
    val view = if (colon >= 0) attributes.substring(0, colon) else "basic"
    if (view.isNotEmpty() && view != "basic") {
        throw UnsupportedOperationException("View '$view' is not supported")
    }
    val list = if (colon >= 0) attributes.substring(colon + 1) else attributes
    if (list == "*" || list.isEmpty()) return ALL_BASIC_ATTRIBUTES
    return list.split(',').map { it.trim() }.filter { it.isNotEmpty() }
}
