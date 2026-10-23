package java.io

actual abstract class InputStream actual constructor() : Closeable {

    actual abstract fun read(): Int

    actual open fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    actual open fun read(b: ByteArray, off: Int, len: Int): Int {
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        } else if (len == 0) {
            return 0
        }

        val c = read()
        if (c == -1) {
            return -1
        }
        b[off] = c.toByte()

        var i = 1
        try {
            while (i < len) {
                val nextC = read()
                if (nextC == -1) {
                    break
                }
                b[off + i] = nextC.toByte()
                i++
            }
        } catch (e: IOException) {
            // Ignore exception if at least 1 byte was read successfully
        }
        return i
    }

    actual open fun skip(n: Long): Long {
        if (n <= 0) return 0L
        val size = minOf(2048, n.toInt())
        val buffer = ByteArray(size)
        var remaining = n
        while (remaining > 0) {
            val count = read(buffer, 0, minOf(size, remaining.toInt()))
            if (count < 0) break
            remaining -= count
        }
        return n - remaining
    }

    actual open fun available(): Int = 0

    actual override fun close() {}

    actual open fun mark(readlimit: Int) {}

    actual open fun reset() {
        throw IOException("mark/reset not supported")
    }

    actual open fun markSupported(): Boolean = false
}
