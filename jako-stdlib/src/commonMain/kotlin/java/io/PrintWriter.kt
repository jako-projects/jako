@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class PrintWriter : Writer {
    constructor(out: OutputStream)
    constructor(out: Writer)
    constructor(file: File)
    constructor(fileName: String)
    fun print(s: String?)
    fun println(s: String?)
    fun println()
    override fun write(c: Int)
    override fun write(cbuf: CharArray, off: Int, len: Int)
    override fun write(str: String, off: Int, len: Int)
    override fun flush()
    override fun close()
}
