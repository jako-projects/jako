@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

expect class CRC32() : Checksum {
    override fun update(b: Int)
    override fun update(b: ByteArray, off: Int, len: Int)
    fun update(b: ByteArray)
    override fun getValue(): Long
    override val value: Long
    override fun reset()
}
