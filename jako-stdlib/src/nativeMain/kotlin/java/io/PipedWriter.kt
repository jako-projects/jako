package java.io

actual open class PipedWriter : Writer {
    private var sink: PipedReader? = null

    actual constructor(snk: PipedReader) {
        connect(snk)
    }

    actual constructor()

    actual open fun connect(snk: PipedReader) {
        if (sink != null || snk.connected) {
            throw IOException("Already connected")
        }
        sink = snk
        snk.initPipe(this)
    }

    actual override fun write(c: Int) {
        val snk = sink ?: throw IOException("Pipe not connected")
        snk.receive(c)
    }

    actual override fun write(cbuf: CharArray, off: Int, len: Int) {
        val snk = sink ?: throw IOException("Pipe not connected")
        snk.receive(cbuf, off, len)
    }

    actual override fun flush() {}

    actual override fun close() {
        sink?.receivedLast()
    }
}
