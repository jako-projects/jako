@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class CharArrayWriter : Writer {
    constructor()
    constructor(initialSize: Int)
    override fun write(c: Int)
    override fun write(cbuf: CharArray, off: Int, len: Int)
    override fun write(str: String, off: Int, len: Int)
    open fun writeTo(out: Writer)
    open fun toCharArray(): CharArray
    open fun reset()
    open fun size(): Int
    override fun toString(): String
    override fun flush()
    override fun close()
}
