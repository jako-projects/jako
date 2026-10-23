@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class FilterInputStream : InputStream {
    protected constructor(inStream: InputStream)
    override fun read(): Int
    override fun read(b: ByteArray): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun skip(n: Long): Long
    override fun available(): Int
    override fun close()
    override fun mark(readlimit: Int)
    override fun reset()
    override fun markSupported(): Boolean
}
