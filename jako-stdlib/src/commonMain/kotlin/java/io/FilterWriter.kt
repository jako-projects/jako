@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class FilterWriter : Writer {
    protected constructor(out: Writer)
    override fun write(c: Int)
    override fun write(cbuf: CharArray, off: Int, len: Int)
    override fun write(str: String, off: Int, len: Int)
    override fun flush()
    override fun close()
}
