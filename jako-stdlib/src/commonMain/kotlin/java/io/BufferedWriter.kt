@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class BufferedWriter : Writer {
    constructor(out: Writer)
    constructor(out: Writer, sz: Int)
    override fun write(c: Int)
    override fun write(cbuf: CharArray, off: Int, len: Int)
    override fun write(str: String, off: Int, len: Int)
    open fun newLine()
    override fun flush()
    override fun close()
}
