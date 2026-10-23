@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class RandomAccessFile : DataOutput, DataInput, Closeable {
    constructor(file: File, mode: String)
    constructor(name: String, mode: String)
    open fun read(): Int
    open fun read(b: ByteArray, off: Int, len: Int): Int
    open fun read(b: ByteArray): Int
    override fun readFully(b: ByteArray)
    override fun readFully(b: ByteArray, off: Int, len: Int)
    override fun skipBytes(n: Int): Int
    override fun write(b: Int)
    override fun write(b: ByteArray)
    override fun write(b: ByteArray, off: Int, len: Int)
    open fun getFilePointer(): Long
    open fun seek(pos: Long)
    open fun length(): Long
    open fun setLength(newLength: Long)
    override fun close()
    override fun readBoolean(): Boolean
    override fun readByte(): Byte
    override fun readUnsignedByte(): Int
    override fun readShort(): Short
    override fun readUnsignedShort(): Int
    override fun readChar(): Char
    override fun readInt(): Int
    override fun readLong(): Long
    override fun readFloat(): Float
    override fun readDouble(): Double
    override fun readLine(): String?
    override fun readUTF(): String
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
    open fun getChannel(): java.nio.channels.FileChannel
    open val channel: java.nio.channels.FileChannel
    fun getFD(): FileDescriptor
    val fd: FileDescriptor
    open val filePointer: Long
}
