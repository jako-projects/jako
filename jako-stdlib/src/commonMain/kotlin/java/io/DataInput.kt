@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect interface DataInput {
    fun readFully(b: ByteArray)
    fun readFully(b: ByteArray, off: Int, len: Int)
    fun skipBytes(n: Int): Int
    fun readBoolean(): Boolean
    fun readByte(): Byte
    fun readUnsignedByte(): Int
    fun readShort(): Short
    fun readUnsignedShort(): Int
    fun readChar(): Char
    fun readInt(): Int
    fun readLong(): Long
    fun readFloat(): Float
    fun readDouble(): Double
    fun readLine(): String?
    fun readUTF(): String
}
