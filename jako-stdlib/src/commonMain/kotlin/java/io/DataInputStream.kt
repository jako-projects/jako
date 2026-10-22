@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class DataInputStream(inStream: InputStream) : FilterInputStream, DataInput {
    override fun read(b: ByteArray): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun readFully(b: ByteArray)
    override fun readFully(b: ByteArray, off: Int, len: Int)
    override fun skipBytes(n: Int): Int
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
}
