@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class ByteArrayOutputStream : OutputStream {
    constructor()
    constructor(size: Int)
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    open fun toByteArray(): ByteArray
    open fun reset()
    open fun size(): Int
}
