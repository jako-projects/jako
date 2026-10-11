package java.io

import java.nio.charset.Charset

actual open class OutputStreamWriter : Writer {
    private val out: OutputStream
    private val encodingName: String

    actual constructor(out: OutputStream) : super() {
        this.out = out
        this.encodingName = "UTF-8"
    }

    actual constructor(out: OutputStream, charsetName: String) : super() {
        this.out = out
        this.encodingName = charsetName
    }

    actual constructor(out: OutputStream, cs: Charset) : super() {
        this.out = out
        this.encodingName = cs.name()
    }

    fun getEncoding(): String? = encodingName

    actual override fun write(c: Int) {
        val cb = CharArray(1)
        cb[0] = c.toChar()
        write(cb, 0, 1)
    }

    actual override fun write(cbuf: CharArray, off: Int, len: Int) {
        if (off < 0 || len < 0 || len > cbuf.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, cbuf.size=${cbuf.size}")
        }
        if (len == 0) return

        val s = cbuf.concatToString(off, off + len)
        val bytes = s.encodeToByteArray()
        out.write(bytes)
    }

    actual override fun write(str: String, off: Int, len: Int) {
        val s = str.substring(off, off + len)
        val bytes = s.encodeToByteArray()
        out.write(bytes)
    }

    actual override fun flush() = out.flush()

    actual override fun close() = out.close()
}
