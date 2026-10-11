package java.io

actual open class FilterOutputStream actual constructor(
    protected val out: OutputStream
) : OutputStream() {
    actual override fun write(b: Int) {
        out.write(b)
    }
    actual override fun write(b: ByteArray) {
        write(b, 0, b.size)
    }
    actual override fun write(b: ByteArray, off: Int, len: Int) {
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        for (i in 0 until len) {
            write(b[off + i].toInt())
        }
    }
    actual override fun flush() {
        out.flush()
    }
    actual override fun close() {
        try {
            flush()
        } catch (ignored: Throwable) {
        }
        out.close()
    }
}
