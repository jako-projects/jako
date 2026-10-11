package java.io

actual open class PipedOutputStream : OutputStream {
    private var sink: PipedInputStream? = null

    actual constructor(snk: PipedInputStream) {
        connect(snk)
    }

    actual constructor()

    actual open fun connect(snk: PipedInputStream) {
        if (sink != null || snk.connected) {
            throw IOException("Already connected")
        }
        sink = snk
        snk.initPipe(this)
    }

    actual override fun write(b: Int) {
        val snk = sink ?: throw IOException("Pipe not connected")
        snk.receive(b)
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        val snk = sink ?: throw IOException("Pipe not connected")
        snk.receive(b, off, len)
    }

    actual override fun flush() {}

    actual override fun close() {
        sink?.receivedLast()
    }
}
