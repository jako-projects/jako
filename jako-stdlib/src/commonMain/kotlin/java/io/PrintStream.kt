@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class PrintStream : FilterOutputStream, Appendable, Closeable {
    constructor(out: OutputStream)
    constructor(out: OutputStream, autoFlush: Boolean)
    constructor(file: File)
    constructor(fileName: String)
    fun print(s: String?)
    fun println(s: String?)
    fun println()
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun flush()
    override fun close()
    override fun append(value: CharSequence?): PrintStream
    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): PrintStream
    override fun append(value: Char): PrintStream
}
