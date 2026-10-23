@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class SequenceInputStream : InputStream {
    constructor(e: java.util.Enumeration<out InputStream>)
    constructor(s1: InputStream, s2: InputStream)
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun available(): Int
    override fun close()
}
