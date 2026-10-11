package java.io

actual open class BufferedReader : Reader {
    private val inReader: Reader
    private var cb: CharArray
    private var nChars: Int = 0
    private var nextChar: Int = 0
    private var markedChar: Int = -1
    private var readAheadLimit: Int = 0

    actual constructor(inReader: Reader) : this(inReader, 8192)

    actual constructor(inReader: Reader, sz: Int) : super(inReader) {
        if (sz <= 0) throw IllegalArgumentException("Buffer size <= 0")
        this.inReader = inReader
        this.cb = CharArray(sz)
    }

    private fun fill() {
        if (markedChar < 0) {
            nextChar = 0
        } else if (nextChar >= cb.size) {
            if (nextChar - markedChar > readAheadLimit) {
                markedChar = -1
                nextChar = 0
            } else {
                val newLen = cb.size * 2
                val newCb = CharArray(newLen)
                cb.copyInto(newCb, 0, markedChar, cb.size)
                nextChar -= markedChar
                markedChar = 0
                cb = newCb
            }
        }
        nChars = nextChar
        val n = inReader.read(cb, nextChar, cb.size - nextChar)
        if (n > 0) nChars = nextChar + n
    }

    actual override fun read(): Int {
        if (nextChar >= nChars) {
            fill()
            if (nextChar >= nChars) return -1
        }
        return cb[nextChar++].code
    }

    actual override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        if (off < 0 || len < 0 || len > cbuf.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, cbuf.size=${cbuf.size}")
        }
        if (len == 0) return 0

        var avail = nChars - nextChar
        if (avail <= 0) {
            fill()
            avail = nChars - nextChar
            if (avail <= 0) return -1
        }

        val cnt = minOf(len, avail)
        cb.copyInto(cbuf, off, nextChar, nextChar + cnt)
        nextChar += cnt
        return cnt
    }

    actual open fun readLine(): String? {
        val sb = StringBuilder()
        var eol = false

        while (true) {
            if (nextChar >= nChars) {
                fill()
            }
            if (nextChar >= nChars) {
                return if (sb.isNotEmpty()) sb.toString() else null
            }

            var omitLf = false
            var i = nextChar
            while (i < nChars) {
                val ch = cb[i]
                if (ch == '\n' || ch == '\r') {
                    eol = true
                    if (ch == '\r') omitLf = true
                    break
                }
                i++
            }

            sb.appendRange(cb, nextChar, i)
            nextChar = i
            if (eol) {
                nextChar++
                if (omitLf) {
                    if (nextChar >= nChars) fill()
                    if (nextChar < nChars && cb[nextChar] == '\n') {
                        nextChar++
                    }
                }
                return sb.toString()
            }
        }
    }

    override fun skip(n: Long): Long {
        if (n < 0L) throw IllegalArgumentException("skip value is negative")
        var r = n
        while (r > 0) {
            if (nextChar >= nChars) {
                fill()
            }
            if (nextChar >= nChars) break
            val avail = nChars - nextChar
            val d = minOf(r.toInt(), avail)
            nextChar += d
            r -= d
        }
        return n - r
    }

    override fun ready(): Boolean {
        return (nextChar < nChars) || inReader.ready()
    }

    override fun markSupported(): Boolean = true

    override fun mark(readAheadLimit: Int) {
        if (readAheadLimit < 0) throw IllegalArgumentException("Read-ahead limit < 0")
        this.readAheadLimit = readAheadLimit
        this.markedChar = nextChar
    }

    override fun reset() {
        if (markedChar < 0) throw IOException("Mark invalid")
        nextChar = markedChar
    }

    actual override fun close() {
        inReader.close()
    }
}
