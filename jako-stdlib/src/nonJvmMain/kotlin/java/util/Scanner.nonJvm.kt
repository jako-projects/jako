package java.util

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Path
import java.util.regex.Pattern

actual open class Scanner : Iterator<String>, Closeable {
    private val buffer: String
    private var position: Int = 0
    private var delimPattern: Pattern = Pattern.compile("\\s+")
    private var closed: Boolean = false

    actual constructor(source: java.lang.Readable) : this(readReadable(source))

    actual constructor(source: String) {
        this.buffer = source
    }

    actual constructor(source: InputStream) : this(readStream(source, null))

    actual constructor(source: InputStream, charsetName: String) : this(readStream(source, charsetName))

    actual constructor(source: File) : this(FileInputStream(source))

    actual constructor(source: File, charsetName: String) : this(FileInputStream(source), charsetName)

    actual constructor(source: Path) : this(source.toFile())

    actual constructor(source: Path, charsetName: String) : this(source.toFile(), charsetName)

    private fun checkClosed() {
        if (closed) throw IllegalStateException("Scanner closed")
    }

    private fun findNextToken(): Pair<Int, Int>? {
        checkClosed()
        var pos = position

        // Skip leading delimiters
        while (pos < buffer.length) {
            val matcher = delimPattern.matcher(buffer)
            if (matcher.find(pos) && matcher.start() == pos) {
                pos = matcher.end()
            } else {
                break
            }
        }

        if (pos >= buffer.length) return null

        // Find end of token (next delimiter)
        val matcher = delimPattern.matcher(buffer)
        val end = if (matcher.find(pos)) {
            matcher.start()
        } else {
            buffer.length
        }

        return Pair(pos, end)
    }

    actual override fun hasNext(): Boolean {
        return findNextToken() != null
    }

    actual override fun next(): String {
        val tokenRange = findNextToken() ?: throw NoSuchElementException()
        position = tokenRange.second
        return buffer.substring(tokenRange.first, tokenRange.second)
    }

    actual open fun hasNextLine(): Boolean {
        checkClosed()
        return position < buffer.length
    }

    actual open fun nextLine(): String {
        checkClosed()
        if (position >= buffer.length) throw NoSuchElementException()
        val nl = buffer.indexOf('\n', position)
        return if (nl >= 0) {
            val line = if (nl > position && buffer[nl - 1] == '\r') {
                buffer.substring(position, nl - 1)
            } else {
                buffer.substring(position, nl)
            }
            position = nl + 1
            line
        } else {
            val line = buffer.substring(position)
            position = buffer.length
            line
        }
    }

    actual open fun hasNextInt(): Boolean {
        val tokenRange = findNextToken() ?: return false
        val token = buffer.substring(tokenRange.first, tokenRange.second)
        return token.toIntOrNull() != null
    }

    actual open fun nextInt(): Int {
        val token = next()
        return token.toIntOrNull() ?: throw InputMismatchException("Not an integer: $token")
    }

    actual open fun hasNextLong(): Boolean {
        val tokenRange = findNextToken() ?: return false
        val token = buffer.substring(tokenRange.first, tokenRange.second)
        return token.toLongOrNull() != null
    }

    actual open fun nextLong(): Long {
        val token = next()
        return token.toLongOrNull() ?: throw InputMismatchException("Not a long: $token")
    }

    actual open fun hasNextDouble(): Boolean {
        val tokenRange = findNextToken() ?: return false
        val token = buffer.substring(tokenRange.first, tokenRange.second)
        return token.toDoubleOrNull() != null
    }

    actual open fun nextDouble(): Double {
        val token = next()
        return token.toDoubleOrNull() ?: throw InputMismatchException("Not a double: $token")
    }

    actual open fun hasNextBoolean(): Boolean {
        val tokenRange = findNextToken() ?: return false
        val token = buffer.substring(tokenRange.first, tokenRange.second)
        return token.equals("true", ignoreCase = true) || token.equals("false", ignoreCase = true)
    }

    actual open fun nextBoolean(): Boolean {
        val token = next()
        return if (token.equals("true", ignoreCase = true)) true
        else if (token.equals("false", ignoreCase = true)) false
        else throw InputMismatchException("Not a boolean: $token")
    }

    actual open fun useDelimiter(pattern: String): Scanner {
        this.delimPattern = Pattern.compile(pattern)
        return this
    }

    actual open fun useDelimiter(pattern: Pattern): Scanner {
        this.delimPattern = pattern
        return this
    }

    actual open fun delimiter(): Pattern = delimPattern

    actual override fun close() {
        closed = true
    }

    companion object {
        private fun readReadable(source: java.lang.Readable): String {
            val sb = StringBuilder()
            val cb = java.nio.CharBuffer.allocate(1024)
            while (true) {
                cb.clear()
                val n = source.read(cb)
                if (n < 0) break
                cb.flip()
                while (cb.hasRemaining()) {
                    sb.append(cb.get())
                }
            }
            return sb.toString()
        }

        private fun readStream(stream: InputStream, charsetName: String?): String {
            val bos = ByteArrayOutputStream()
            val buf = ByteArray(8192)
            while (true) {
                val n = stream.read(buf)
                if (n <= 0) break
                bos.write(buf, 0, n)
            }
            val bytes = bos.toByteArray()
            return bytes.decodeToString()
        }
    }
}
