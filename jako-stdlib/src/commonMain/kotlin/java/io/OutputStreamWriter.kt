@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class OutputStreamWriter : Writer {
    constructor(out: OutputStream)
    constructor(out: OutputStream, charsetName: String)
    constructor(out: OutputStream, cs: java.nio.charset.Charset)
    override fun write(c: Int)
    override fun write(cbuf: CharArray, off: Int, len: Int)
    override fun write(str: String, off: Int, len: Int)
    override fun flush()
    override fun close()
}
