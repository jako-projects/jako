@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

expect interface Checksum {
    fun update(b: Int)
    fun update(b: ByteArray, off: Int, len: Int)
    fun getValue(): Long
    val value: Long
    fun reset()
}
