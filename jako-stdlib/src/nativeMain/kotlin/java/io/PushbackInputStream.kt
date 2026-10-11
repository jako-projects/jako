package java.io

actual open class PushbackInputStream : FilterInputStream {
    protected var buf: ByteArray
    protected var pos: Int

    actual constructor(inStream: InputStream, size: Int) : super(inStream) {
        if (size <= 0) {
            throw IllegalArgumentException("Buffer size <= 0")
        }
        buf = ByteArray(size)
        pos = size
    }

    actual constructor(inStream: InputStream) : this(inStream, 1)

    private fun ensureOpen() {
        if (`in` == null) {
            throw IOException("Stream closed")
        }
    }

    override fun read(): Int {
        ensureOpen()
        return if (pos < buf.size) {
            buf[pos++].toInt() and 0xFF
        } else {
            super.read()
        }
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        ensureOpen()
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0

        var currentOff = off
        var currentLen = len
        var bytesRead = 0

        val avail = buf.size - pos
        if (avail > 0) {
            val cnt = minOf(avail, currentLen)
            buf.copyInto(b, currentOff, pos, pos + cnt)
            pos += cnt
            currentOff += cnt
            currentLen -= cnt
            bytesRead += cnt
        }

        if (currentLen > 0) {
            val n = super.read(b, currentOff, currentLen)
            if (n == -1) {
                return if (bytesRead == 0) -1 else bytesRead
            }
            bytesRead += n
        }

        return bytesRead
    }

    actual open fun unread(b: Int) {
        ensureOpen()
        if (pos == 0) {
            throw IOException("Push back buffer is full")
        }
        buf[--pos] = b.toByte()
    }

    actual open fun unread(b: ByteArray, off: Int, len: Int) {
        ensureOpen()
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len > pos) {
            throw IOException("Push back buffer is full")
        }
        pos -= len
        b.copyInto(buf, pos, off, off + len)
    }

    actual open fun unread(b: ByteArray) {
        unread(b, 0, b.size)
    }

    override fun available(): Int {
        ensureOpen()
        val n = buf.size - pos
        val p = super.available()
        return if (n > Int.MAX_VALUE - p) Int.MAX_VALUE else n + p
    }

    override fun skip(n: Long): Long {
        ensureOpen()
        if (n <= 0L) return 0L
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

    override fun markSupported(): Boolean = false

    override fun mark(readlimit: Int) {}

    override fun reset() {
        throw IOException("mark/reset not supported")
    }

    override fun close() {
        super.close()
        buf = ByteArray(0)
        pos = 0
    }
}
