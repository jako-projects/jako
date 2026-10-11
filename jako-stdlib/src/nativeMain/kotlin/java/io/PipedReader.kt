package java.io

actual open class PipedReader : Reader {
    internal var closedByWriter = false
    internal var closedByReader = false
    internal var connected = false

    protected var buffer: CharArray
    protected var `in`: Int = -1
    protected var `out`: Int = 0

    companion object {
        const val PIPE_SIZE = 1024
    }

    actual constructor(src: PipedWriter) : this(src, PIPE_SIZE)

    actual constructor(src: PipedWriter, pipeSize: Int) : this(pipeSize) {
        connect(src)
    }

    actual constructor() : this(PIPE_SIZE)

    actual constructor(pipeSize: Int) {
        if (pipeSize <= 0) {
            throw IllegalArgumentException("Pipe Size <= 0")
        }
        buffer = CharArray(pipeSize)
    }

    actual open fun connect(src: PipedWriter) {
        src.connect(this)
    }

    internal fun initPipe(src: PipedWriter) {
        if (connected) {
            throw IOException("Already connected")
        }
        connected = true
        `in` = -1
        `out` = 0
    }

    internal fun receive(c: Int) {
        checkStateForReceive()
        if (`in` < 0) {
            `in` = 0
            `out` = 0
        } else if (`in` == `out`) {
            val newBuf = CharArray(buffer.size * 2)
            buffer.copyInto(newBuf, 0, 0, buffer.size)
            `in` = buffer.size
            `out` = 0
            buffer = newBuf
        }
        buffer[`in`++] = c.toChar()
        if (`in` >= buffer.size) {
            `in` = 0
        }
    }

    internal fun receive(cbuf: CharArray, off: Int, len: Int) {
        checkStateForReceive()
        for (i in 0 until len) {
            receive(cbuf[off + i].code)
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
        val r = buffer[`out`++].code
        if (`out` >= buffer.size) {
            `out` = 0
        }
        if (`in` == `out`) {
            `in` = -1
        }
        return r
    }

    actual override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        if (!connected) throw IOException("Pipe not connected")
        if (closedByReader) throw IOException("Pipe closed")
        if (off < 0 || len < 0 || len > cbuf.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, cbuf.size=${cbuf.size}")
        }
        if (len == 0) return 0

        val c = read()
        if (c < 0) return -1
        cbuf[off] = c.toChar()
        var rlen = 1
        while (rlen < len && `in` >= 0) {
            val c1 = read()
            if (c1 < 0) break
            cbuf[off + rlen] = c1.toChar()
            rlen++
        }
        return rlen
    }

    actual override fun ready(): Boolean {
        if (!connected || closedByReader) return false
        return `in` >= 0
    }

    actual override fun close() {
        closedByReader = true
        `in` = -1
    }
}
