package java.io

actual open class BufferedInputStream : FilterInputStream {
    private var buf: ByteArray
    private var count: Int = 0
    private var pos: Int = 0
    private var markpos: Int = -1
    private var marklimit: Int = 0

    actual constructor(inStream: InputStream) : this(inStream, 8192)

    actual constructor(inStream: InputStream, size: Int) : super(inStream) {
        if (size <= 0) throw IllegalArgumentException("Buffer size <= 0")
        this.buf = ByteArray(size)
    }

    private fun fill() {
        if (markpos < 0) {
            pos = 0
        } else if (pos >= buf.size) {
            if (pos - markpos > marklimit) {
                markpos = -1
                pos = 0
            } else {
                val newBuf = ByteArray(buf.size * 2)
                buf.copyInto(newBuf, 0, markpos, buf.size)
                pos -= markpos
                markpos = 0
                buf = newBuf
            }
        }
        count = pos
        val n = inStream.read(buf, pos, buf.size - pos)
        if (n > 0) count = pos + n
    }

    actual override fun read(): Int {
        if (pos >= count) {
            fill()
            if (pos >= count) return -1
        }
        return buf[pos++].toInt() and 0xFF
    }

    actual override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0

        var avail = count - pos
        if (avail <= 0) {
            if (len >= buf.size && markpos < 0) {
                return inStream.read(b, off, len)
            }
            fill()
            avail = count - pos
            if (avail <= 0) return -1
        }

        val cnt = minOf(len, avail)
        buf.copyInto(b, off, pos, pos + cnt)
        pos += cnt
        return cnt
    }

    override fun skip(n: Long): Long {
        if (n <= 0L) return 0L
        var avail = (count - pos).toLong()
        if (avail <= 0) {
            if (markpos < 0) {
                return inStream.skip(n)
            }
            fill()
            avail = (count - pos).toLong()
            if (avail <= 0) return 0L
        }
        val skipped = minOf(avail, n)
        pos += skipped.toInt()
        return skipped
    }

    override fun available(): Int {
        val n = count - pos
        val avail = inStream.available()
        return if (n > Int.MAX_VALUE - avail) Int.MAX_VALUE else n + avail
    }

    override fun mark(readlimit: Int) {
        marklimit = readlimit
        markpos = pos
    }

    override fun reset() {
        if (markpos < 0) throw IOException("Resetting to invalid mark")
        pos = markpos
    }

    override fun markSupported(): Boolean = true

    override fun close() {
        inStream.close()
    }
}
