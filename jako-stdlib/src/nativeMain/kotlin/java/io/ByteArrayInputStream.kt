package java.io

actual open class ByteArrayInputStream : InputStream {
    protected val buf: ByteArray
    protected var pos: Int
    protected var mark: Int = 0
    protected var count: Int

    actual constructor(buf: ByteArray) : super() {
        this.buf = buf
        this.pos = 0
        this.count = buf.size
        this.mark = 0
    }

    actual constructor(buf: ByteArray, offset: Int, length: Int) : super() {
        this.buf = buf
        this.pos = offset
        this.count = minOf(offset + length, buf.size)
        this.mark = offset
    }

    actual override fun read(): Int {
        return if (pos < count) (buf[pos++].toInt() and 0xFF) else -1
    }

    actual override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (pos >= count) return -1
        val avail = count - pos
        var readLen = len
        if (readLen > avail) {
            readLen = avail
        }
        if (readLen <= 0) return 0

        buf.copyInto(b, destinationOffset = off, startIndex = pos, endIndex = pos + readLen)
        pos += readLen
        return readLen
    }

    actual override fun skip(n: Long): Long {
        var k = (count - pos).toLong()
        if (n < k) {
            k = if (n < 0) 0L else n
        }
        pos += k.toInt()
        return k
    }

    actual override fun available(): Int = count - pos

    override fun markSupported(): Boolean = true

    override fun mark(readlimit: Int) {
        mark = pos
    }

    override fun reset() {
        pos = mark
    }

    override fun close() {}
}
