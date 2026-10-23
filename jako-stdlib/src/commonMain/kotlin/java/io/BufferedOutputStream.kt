@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class BufferedOutputStream : FilterOutputStream {
    constructor(out: OutputStream)
    constructor(out: OutputStream, size: Int)
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
}
