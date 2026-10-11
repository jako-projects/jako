package java.io

actual open class LineNumberReader : BufferedReader {
    private var _lineNumber: Int = 0
    private var markedLineNumber: Int = 0
    private var skipLF: Boolean = false
    private var markedSkipLF: Boolean = false

    actual constructor(inReader: Reader) : super(inReader)

    actual constructor(inReader: Reader, sz: Int) : super(inReader, sz)

    actual open fun setLineNumber(lineNumber: Int) {
        this._lineNumber = lineNumber
    }

    actual open fun getLineNumber(): Int {
        return _lineNumber
    }

    actual open val lineNumber: Int get() = getLineNumber()

    override fun read(): Int {
        var c = super.read()
        if (skipLF) {
            if (c == '\n'.code) {
                c = super.read()
            }
            skipLF = false
        }
        when (c) {
            '\r'.code -> {
                skipLF = true
                _lineNumber++
                return '\n'.code
            }
            '\n'.code -> {
                _lineNumber++
            }
        }
        return c
    }

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        if (off < 0 || len < 0 || len > cbuf.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, cbuf.size=${cbuf.size}")
        }
        if (len == 0) return 0

        val c = read()
        if (c == -1) return -1
        cbuf[off] = c.toChar()

        var i = 1
        while (i < len) {
            val ch = read()
            if (ch == -1) break
            cbuf[off + i] = ch.toChar()
            i++
        }
        return i
    }

    override fun readLine(): String? {
        val l = super.readLine() ?: return null
        _lineNumber++
        return l
    }

    override fun skip(n: Long): Long {
        if (n < 0L) throw IllegalArgumentException("skip() value is negative")
        val bufferSize = 8192
        val skipBuffer = CharArray(bufferSize)
        var r = n
        while (r > 0) {
            val nc = read(skipBuffer, 0, minOf(r.toInt(), bufferSize))
            if (nc == -1) break
            r -= nc
        }
        return n - r
    }

    override fun mark(readAheadLimit: Int) {
        super.mark(readAheadLimit)
        markedLineNumber = _lineNumber
        markedSkipLF = skipLF
    }

    override fun reset() {
        super.reset()
        _lineNumber = markedLineNumber
        skipLF = markedSkipLF
    }
}
