package java.io

actual open class ObjectInputStream : InputStream {
    private val inStream: InputStream?

    actual constructor(inStream: InputStream) {
        this.inStream = inStream
    }

    actual protected constructor() {
        this.inStream = null
    }

    actual open fun readObject(): Any? {
        val s = inStream ?: throw IOException("Stream closed")
        return null
    }

    actual override fun read(): Int {
        return inStream?.read() ?: -1
    }

    actual override fun read(b: ByteArray, off: Int, len: Int): Int {
        return inStream?.read(b, off, len) ?: -1
    }

    actual override fun close() {
        inStream?.close()
    }
}

actual open class ObjectOutputStream : OutputStream {
    private val outStream: OutputStream?

    actual constructor(out: OutputStream) {
        this.outStream = out
    }

    actual protected constructor() {
        this.outStream = null
    }

    actual open fun writeObject(obj: Any?) {
        val out = outStream ?: throw IOException("Stream closed")
        val str = obj?.toString() ?: ""
        val bytes = str.encodeToByteArray()
        out.write(bytes)
    }

    actual override fun write(b: Int) {
        outStream?.write(b)
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        outStream?.write(b, off, len)
    }

    actual override fun flush() {
        outStream?.flush()
    }

    actual override fun close() {
        outStream?.close()
    }
}
