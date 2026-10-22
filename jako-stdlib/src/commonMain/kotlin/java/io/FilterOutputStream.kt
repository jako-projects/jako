@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class FilterOutputStream : OutputStream {
    constructor(out: OutputStream)
    override fun write(b: Int)
    override fun write(b: ByteArray)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun flush()
    override fun close()
}
