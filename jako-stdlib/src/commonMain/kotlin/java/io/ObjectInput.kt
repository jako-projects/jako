@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect interface ObjectInput : DataInput, AutoCloseable {
    fun readObject(): Any?
    fun read(): Int
    fun read(b: ByteArray): Int
    fun read(b: ByteArray, off: Int, len: Int): Int
    fun skip(n: Long): Long
    fun available(): Int
    override fun close()
}
