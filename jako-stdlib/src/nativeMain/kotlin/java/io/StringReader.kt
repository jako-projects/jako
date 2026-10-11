package java.io

actual open class StringReader : Reader {
    private var str: String?
    private var length: Int = 0
    private var next: Int = 0
    private var mark: Int = 0

    actual constructor(s: String) : super() {
        this.str = s
        this.length = s.length
    }

    private fun ensureOpen() {
        if (str == null) throw IOException("Stream closed")
    }

    actual override fun read(): Int {
        ensureOpen()
        val s = str ?: return -1
        return if (next >= length) -1 else s[next++].code
    }

    actual override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        ensureOpen()
        val s = str ?: return -1
        if (off < 0 || len < 0 || len > cbuf.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, cbuf.size=${cbuf.size}")
        }
        if (len == 0) return 0
        if (next >= length) return -1

        val n = minOf(length - next, len)
        for (i in 0 until n) {
            cbuf[off + i] = s[next + i]
        }
        next += n
        return n
    }

    override fun skip(n: Long): Long {
        ensureOpen()
        if (next >= length) return 0L
        var r = minOf((length - next).toLong(), n)
        r = maxOf(-next.toLong(), r)
        next += r.toInt()
        return r
    }

    override fun ready(): Boolean {
        ensureOpen()
        return true
    }

    override fun markSupported(): Boolean = true

    override fun mark(readAheadLimit: Int) {
        if (readAheadLimit < 0) throw IllegalArgumentException("Read-ahead limit < 0")
        ensureOpen()
        mark = next
    }

    override fun reset() {
        ensureOpen()
        next = mark
    }

    actual override fun close() {
        str = null
    }
}
