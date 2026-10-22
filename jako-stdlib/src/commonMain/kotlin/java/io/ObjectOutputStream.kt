@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class ObjectOutputStream : OutputStream, ObjectOutput, ObjectStreamConstants {
    constructor(out: OutputStream)
    protected constructor()
    override fun writeObject(obj: Any?)
    override fun write(b: Int)
    override fun write(b: ByteArray)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun flush()
    open fun reset()
    override fun close()
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
