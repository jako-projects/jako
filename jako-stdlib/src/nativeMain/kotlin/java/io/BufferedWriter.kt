package java.io

actual open class BufferedWriter : Writer {
    private val outWriter: Writer
    private val cb: CharArray
    private var nextChar: Int = 0

    actual constructor(out: Writer) : this(out, 8192)

    actual constructor(out: Writer, sz: Int) : super(out) {
        if (sz <= 0) throw IllegalArgumentException("Buffer size <= 0")
        this.outWriter = out
        this.cb = CharArray(sz)
    }

    private fun flushBuffer() {
        if (nextChar > 0) {
            outWriter.write(cb, 0, nextChar)
            nextChar = 0
        }
    }

    actual override fun write(c: Int) {
        if (nextChar >= cb.size) {
            flushBuffer()
        }
        cb[nextChar++] = c.toChar()
    }

    actual override fun write(cbuf: CharArray, off: Int, len: Int) {
        if (off < 0 || len < 0 || len > cbuf.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, cbuf.size=${cbuf.size}")
        }
        if (len >= cb.size) {
            flushBuffer()
            outWriter.write(cbuf, off, len)
            return
        }
        if (len > cb.size - nextChar) {
            flushBuffer()
        }
        cbuf.copyInto(cb, nextChar, off, off + len)
        nextChar += len
    }

    actual override fun write(str: String, off: Int, len: Int) {
        val cbuf = CharArray(len)
        for (i in 0 until len) {
            cbuf[i] = str[off + i]
        }
        write(cbuf, 0, len)
    }

    actual open fun newLine() {
        write("\n")
    }

    actual override fun flush() {
        flushBuffer()
        outWriter.flush()
    }

    actual override fun close() {
        try {
            flushBuffer()
        } finally {
            outWriter.close()
        }
    }
}
