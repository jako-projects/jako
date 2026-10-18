package java.io

actual abstract class Reader : java.lang.Readable, Closeable {
    protected val lock: Any

    actual protected constructor() {
        this.lock = this
    }

    protected constructor(lock: Any?) {
        this.lock = lock ?: this
    }

    actual override fun read(target: java.nio.CharBuffer): Int {
        val len = target.remaining()
        val cbuf = CharArray(len)
        val n = read(cbuf, 0, len)
        if (n > 0) {
            target.put(cbuf, 0, n)
        }
        return n
    }

    actual open fun read(): Int {
        val cb = CharArray(1)
        return if (read(cb, 0, 1) == -1) -1 else cb[0].code
    }

    actual open fun read(cbuf: CharArray): Int {
        return read(cbuf, 0, cbuf.size)
    }

    actual abstract fun read(cbuf: CharArray, off: Int, len: Int): Int

    actual open fun skip(n: Long): Long {
        if (n < 0L) throw IllegalArgumentException("skip value is negative")
        val nn = minOf(n, 8192L).toInt()
        val skipBuffer = CharArray(nn)
        var r = n
        while (r > 0) {
            val nc = read(skipBuffer, 0, minOf(r.toInt(), nn))
            if (nc == -1) break
            r -= nc
        }
        return n - r
    }

    actual open fun ready(): Boolean = false

    actual open fun markSupported(): Boolean = false

    actual open fun mark(readAheadLimit: Int) {
        throw IOException("mark() not supported")
    }

    actual open fun reset() {
        throw IOException("reset() not supported")
    }

    actual abstract override fun close()
}
