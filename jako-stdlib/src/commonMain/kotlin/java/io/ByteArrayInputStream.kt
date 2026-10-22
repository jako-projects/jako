@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class ByteArrayInputStream : InputStream {
    constructor(buf: ByteArray)
    constructor(buf: ByteArray, offset: Int, length: Int)
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun available(): Int
    override fun skip(n: Long): Long
}
