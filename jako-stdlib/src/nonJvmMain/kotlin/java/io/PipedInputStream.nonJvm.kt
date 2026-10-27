package java.io

actual open class PipedInputStream : InputStream {
    protected var closedByWriter = false
    protected var closedByReader = false
    internal var connected = false

    protected var buffer: ByteArray
    protected var `in`: Int = -1
    protected var `out`: Int = 0

    companion object {
        const val PIPE_SIZE = 1024
    }

    actual constructor(src: PipedOutputStream) : this(src, PIPE_SIZE)

    actual constructor(src: PipedOutputStream, pipeSize: Int) : this(pipeSize) {
        connect(src)
    }

    actual constructor() : this(PIPE_SIZE)

    actual constructor(pipeSize: Int) {
        if (pipeSize <= 0) {
            throw IllegalArgumentException("Pipe Size <= 0")
        }
        buffer = ByteArray(pipeSize)
    }

    actual open fun connect(src: PipedOutputStream) {
        src.connect(this)
    }

    internal fun initPipe(src: PipedOutputStream) {
        if (connected) {
            throw IOException("Already connected")
        }
        connected = true
        `in` = -1
        `out` = 0
    }

    internal open fun receive(b: Int) {
        checkStateForReceive()
        if (`in` < 0) {
            `in` = 0
            `out` = 0
        } else if (`in` == `out`) {
            val newBuf = ByteArray(buffer.size * 2)
            buffer.copyInto(newBuf, 0, 0, buffer.size)
            `in` = buffer.size
            `out` = 0
            buffer = newBuf
        }
        buffer[`in`++] = (b and 0xFF).toByte()
        if (`in` >= buffer.size) {
            `in` = 0
        }
    }

    internal fun receive(b: ByteArray, off: Int, len: Int) {
        checkStateForReceive()
        for (i in 0 until len) {
            receive(b[off + i].toInt())
        }
    }

    private fun checkStateForReceive() {
        if (!connected) {
            throw IOException("Pipe not connected")
        }
        if (closedByReader || closedByWriter) {
            throw IOException("Pipe closed")
        }
    }

    internal fun receivedLast() {
        closedByWriter = true
    }

    actual override fun read(): Int {
        if (!connected) throw IOException("Pipe not connected")
        if (closedByReader) throw IOException("Pipe closed")
        if (`in` < 0) {
            return -1
        }
        val r = buffer[`out`++].toInt() and 0xFF
        if (`out` >= buffer.size) {
            `out` = 0
        }
        if (`in` == `out`) {
            `in` = -1
        }
        return r
    }

    actual override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (!connected) throw IOException("Pipe not connected")
        if (closedByReader) throw IOException("Pipe closed")
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0

        val c = read()
        if (c < 0) return -1
        b[off] = c.toByte()
        var rlen = 1
        while (rlen < len && `in` >= 0) {
            val b1 = read()
            if (b1 < 0) break
            b[off + rlen] = b1.toByte()
            rlen++
        }
        return rlen
    }

    actual override fun available(): Int {
        if (!connected || closedByReader) return 0
        if (`in` < 0) return 0
        return if (`in` == `out`) buffer.size
        else if (`in` > `out`) `in` - `out`
        else buffer.size - `out` + `in`
    }

    actual override fun close() {
        closedByReader = true
        `in` = -1
    }
}
