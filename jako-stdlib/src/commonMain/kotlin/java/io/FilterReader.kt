@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class FilterReader : Reader {
    protected constructor(inReader: Reader)
    override fun read(): Int
    override fun read(cbuf: CharArray): Int
    override fun read(cbuf: CharArray, off: Int, len: Int): Int
    override fun skip(n: Long): Long
    override fun markSupported(): Boolean
    override fun mark(readAheadLimit: Int)
    override fun reset()
    override fun close()
}
