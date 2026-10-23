@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class DataOutputStream(out: OutputStream) : FilterOutputStream, DataOutput {
    fun size(): Int
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun flush()
    override fun writeBoolean(v: Boolean)
    override fun writeByte(v: Int)
    override fun writeShort(v: Int)
    override fun writeChar(v: Int)
    override fun writeInt(v: Int)
    override fun writeLong(v: Long)
    override fun writeFloat(v: Float)
    override fun writeDouble(v: Double)
    override fun writeBytes(s: String)
    override fun writeChars(s: String)
    override fun writeUTF(s: String)
}
