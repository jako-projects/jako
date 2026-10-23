@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class ObjectInputStream : InputStream, ObjectInput, ObjectStreamConstants {
    constructor(inStream: InputStream)
    protected constructor()
    override fun readObject(): Any?
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun available(): Int
    override fun close()
    override fun readBoolean(): Boolean
    override fun readByte(): Byte
    override fun readUnsignedByte(): Int
    override fun readChar(): Char
    override fun readShort(): Short
    override fun readUnsignedShort(): Int
    override fun readInt(): Int
    override fun readLong(): Long
    override fun readFloat(): Float
    override fun readDouble(): Double
    override fun readFully(b: ByteArray)
    override fun readFully(b: ByteArray, off: Int, len: Int)
    override fun skipBytes(n: Int): Int
    override fun readLine(): String?
    override fun readUTF(): String
}
