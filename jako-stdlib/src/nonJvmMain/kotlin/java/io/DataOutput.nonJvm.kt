package java.io

actual interface DataOutput {
    actual fun write(b: Int)
    actual fun write(b: ByteArray)
    actual fun write(b: ByteArray, off: Int, len: Int)
    actual fun writeBoolean(v: Boolean)
    actual fun writeByte(v: Int)
    actual fun writeShort(v: Int)
    actual fun writeChar(v: Int)
    actual fun writeInt(v: Int)
    actual fun writeLong(v: Long)
    actual fun writeFloat(v: Float)
    actual fun writeDouble(v: Double)
    actual fun writeBytes(s: String)
    actual fun writeChars(s: String)
    actual fun writeUTF(s: String)
}
