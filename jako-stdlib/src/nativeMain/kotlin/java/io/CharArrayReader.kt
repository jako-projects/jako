package java.io

actual open class CharArrayReader : Reader {
    protected var buf: CharArray
    protected var pos: Int
    protected var markedPos: Int = 0
    protected var count: Int

    actual constructor(buf: CharArray) : this(buf, 0, buf.size)

    actual constructor(buf: CharArray, offset: Int, length: Int) {
        if (offset < 0 || offset > buf.size || length < 0 || offset + length < 0) {
            throw IllegalArgumentException()
        }
        this.buf = buf
        this.pos = offset
        this.count = minOf(offset + length, buf.size)
        this.markedPos = offset
    }

    private fun ensureOpen() {
        if (buf.isEmpty() && count < 0) {
            throw IOException("Stream closed")
        }
    }

    actual override fun read(): Int {
        ensureOpen()
        return if (pos >= count) -1 else buf[pos++].code
    }

    actual override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        ensureOpen()
        if (off < 0 || len < 0 || len > cbuf.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, cbuf.size=${cbuf.size}")
        }
        if (len == 0) return 0
        if (pos >= count) return -1

        val avail = count - pos
        val cnt = minOf(len, avail)
        buf.copyInto(cbuf, off, pos, pos + cnt)
        pos += cnt
        return cnt
    }

    override fun skip(n: Long): Long {
        ensureOpen()
        val avail = (count - pos).toLong()
        if (avail <= 0) return 0L
        val skipped = minOf(avail, maxOf(0L, n))
        pos += skipped.toInt()
        return skipped
    }

    override fun ready(): Boolean {
        ensureOpen()
        return count - pos > 0
    }

    override fun markSupported(): Boolean = true

    override fun mark(readAheadLimit: Int) {
        ensureOpen()
        markedPos = pos
    }

    override fun reset() {
        ensureOpen()
        pos = markedPos
    }

    actual override fun close() {
        pos = count
    }
}
