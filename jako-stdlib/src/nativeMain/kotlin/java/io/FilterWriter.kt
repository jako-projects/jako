package java.io

actual open class FilterWriter protected actual constructor(
    protected val out: Writer
) : Writer(out) {
    actual override fun write(c: Int) {
        out.write(c)
    }
    actual override fun write(cbuf: CharArray, off: Int, len: Int) {
        out.write(cbuf, off, len)
    }
    actual override fun write(str: String, off: Int, len: Int) {
        out.write(str, off, len)
    }
    actual override fun flush() {
        out.flush()
    }
    actual override fun close() {
        out.close()
    }
}
