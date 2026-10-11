package java.io

actual abstract class Writer : Closeable, Flushable, Appendable {
    protected val lock: Any

    actual protected constructor() {
        this.lock = this
    }

    protected constructor(lock: Any?) {
        this.lock = lock ?: this
    }

    actual open fun write(c: Int) {
        val cb = CharArray(1)
        cb[0] = c.toChar()
        write(cb, 0, 1)
    }

    actual open fun write(cbuf: CharArray) {
        write(cbuf, 0, cbuf.size)
    }

    actual abstract fun write(cbuf: CharArray, off: Int, len: Int)

    actual open fun write(str: String) {
        write(str, 0, str.length)
    }

    actual open fun write(str: String, off: Int, len: Int) {
        val cbuf = CharArray(len)
        for (i in 0 until len) {
            cbuf[i] = str[off + i]
        }
        write(cbuf, 0, len)
    }

    actual override fun append(value: CharSequence?): Writer {
        val s = value?.toString() ?: "null"
        write(s)
        return this
    }

    actual override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Writer {
        val s = (value?.toString() ?: "null").substring(startIndex, endIndex)
        write(s)
        return this
    }

    actual override fun append(value: Char): Writer {
        write(value.code)
        return this
    }

    actual abstract override fun flush()

    actual abstract override fun close()
}
