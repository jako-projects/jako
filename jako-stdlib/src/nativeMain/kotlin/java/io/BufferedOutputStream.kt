package java.io

actual open class BufferedOutputStream : FilterOutputStream {
    private val buf: ByteArray
    private var count: Int = 0

    actual constructor(out: OutputStream) : this(out, 8192)

    actual constructor(out: OutputStream, size: Int) : super(out) {
        if (size <= 0) throw IllegalArgumentException("Buffer size <= 0")
        this.buf = ByteArray(size)
    }

    private fun flushBuffer() {
        if (count > 0) {
            out.write(buf, 0, count)
            count = 0
        }
    }

    actual override fun write(b: Int) {
        if (count >= buf.size) {
            flushBuffer()
        }
        buf[count++] = b.toByte()
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len >= buf.size) {
            flushBuffer()
            out.write(b, off, len)
            return
        }
        if (len > buf.size - count) {
            flushBuffer()
        }
        b.copyInto(buf, count, off, off + len)
        count += len
    }

    override fun flush() {
        flushBuffer()
        out.flush()
    }

    override fun close() {
        try {
            flushBuffer()
        } finally {
            out.close()
        }
    }
}
