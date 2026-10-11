package java.io

actual open class PushbackReader : FilterReader {
    protected var buf: CharArray
    protected var pos: Int

    actual constructor(inReader: Reader, size: Int) : super(inReader) {
        if (size <= 0) {
            throw IllegalArgumentException("Buffer size <= 0")
        }
        buf = CharArray(size)
        pos = size
    }

    actual constructor(inReader: Reader) : this(inReader, 1)

    private fun ensureOpen() {
    }

    override fun read(): Int {
        ensureOpen()
        return if (pos < buf.size) {
            buf[pos++].code
        } else {
            super.read()
        }
    }

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        ensureOpen()
        if (off < 0 || len < 0 || len > cbuf.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, cbuf.size=${cbuf.size}")
        }
        if (len == 0) return 0

        var currentOff = off
        var currentLen = len
        var charsRead = 0

        val avail = buf.size - pos
        if (avail > 0) {
            val cnt = minOf(avail, currentLen)
            buf.copyInto(cbuf, currentOff, pos, pos + cnt)
            pos += cnt
            currentOff += cnt
            currentLen -= cnt
            charsRead += cnt
        }

        if (currentLen > 0) {
            val n = super.read(cbuf, currentOff, currentLen)
            if (n == -1) {
                return if (charsRead == 0) -1 else charsRead
            }
            charsRead += n
        }

        return charsRead
    }

    actual open fun unread(c: Int) {
        ensureOpen()
        if (pos == 0) {
            throw IOException("Push back buffer is full")
        }
        buf[--pos] = c.toChar()
    }

    actual open fun unread(cbuf: CharArray, off: Int, len: Int) {
        ensureOpen()
        if (off < 0 || len < 0 || len > cbuf.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, cbuf.size=${cbuf.size}")
        }
        if (len > pos) {
            throw IOException("Push back buffer is full")
        }
        pos -= len
        cbuf.copyInto(buf, pos, off, off + len)
    }

    actual open fun unread(cbuf: CharArray) {
        unread(cbuf, 0, cbuf.size)
    }

    override fun ready(): Boolean {
        ensureOpen()
        return (buf.size - pos > 0) || super.ready()
    }

    override fun markSupported(): Boolean = false

    override fun mark(readAheadLimit: Int) {
        throw IOException("mark/reset not supported")
    }

    override fun reset() {
        throw IOException("mark/reset not supported")
    }

    override fun skip(n: Long): Long {
        if (n < 0L) throw IllegalArgumentException("skip value is negative")
        ensureOpen()
        if (n == 0L) return 0L

        var p = n
        val avail = (buf.size - pos).toLong()
        if (avail > 0) {
            val cnt = minOf(avail, p)
            pos += cnt.toInt()
            p -= cnt
        }
        if (p > 0) {
            p -= super.skip(p)
        }
        return n - p
    }

    override fun close() {
        super.close()
        buf = CharArray(0)
        pos = 0
    }
}
