package java.io

actual interface ObjectInput : DataInput, AutoCloseable {
    actual fun readObject(): Any?
    actual fun read(): Int
    actual fun read(b: ByteArray): Int
    actual fun read(b: ByteArray, off: Int, len: Int): Int
    actual fun skip(n: Long): Long
    actual fun available(): Int
    actual override fun close()
}
