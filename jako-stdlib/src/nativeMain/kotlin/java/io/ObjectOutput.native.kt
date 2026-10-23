package java.io

actual interface ObjectOutput : DataOutput, AutoCloseable {
    actual fun writeObject(obj: Any?)
    actual override fun write(b: Int)
    actual override fun write(b: ByteArray)
    actual override fun write(b: ByteArray, off: Int, len: Int)
    @Throws(IOException::class)
    actual fun flush()
    actual override fun close()
}
