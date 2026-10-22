package java.util.zip

actual interface Checksum {
    actual fun update(b: Int)
    actual fun update(b: ByteArray, off: Int, len: Int)
    actual fun getValue(): Long
    actual val value: Long
    actual fun reset()
}
