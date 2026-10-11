package java.io

actual open class StringWriter : Writer {
    private val sb: StringBuilder

    actual constructor() : super() {
        this.sb = StringBuilder()
    }

    constructor(initialSize: Int) : super() {
        if (initialSize < 0) throw IllegalArgumentException("Negative initial size: $initialSize")
        this.sb = StringBuilder(initialSize)
    }

    actual override fun write(c: Int) {
        sb.append(c.toChar())
    }

    actual override fun write(cbuf: CharArray, off: Int, len: Int) {
        if (off < 0 || len < 0 || len > cbuf.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, cbuf.size=${cbuf.size}")
        }
        if (len == 0) return
        sb.appendRange(cbuf, off, off + len)
    }

    actual override fun write(str: String) {
        sb.append(str)
    }

    actual override fun write(str: String, off: Int, len: Int) {
        sb.append(str, off, off + len)
    }

    actual override fun append(value: CharSequence?): StringWriter {
        val s = value?.toString() ?: "null"
        write(s)
        return this
    }

    actual override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): StringWriter {
        val s = (value?.toString() ?: "null").substring(startIndex, endIndex)
        write(s)
        return this
    }

    actual override fun append(value: Char): StringWriter {
        write(value.code)
        return this
    }

    actual override fun toString(): String = sb.toString()

    actual override fun flush() {}

    actual override fun close() {}
}
