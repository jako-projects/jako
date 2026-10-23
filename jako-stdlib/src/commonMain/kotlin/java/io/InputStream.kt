@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect abstract class InputStream : Closeable {
    constructor()
    abstract fun read(): Int
    open fun read(b: ByteArray): Int
    open fun read(b: ByteArray, off: Int, len: Int): Int
    open fun skip(n: Long): Long
    open fun available(): Int
    override fun close()
    open fun mark(readlimit: Int)
    open fun reset()
    open fun markSupported(): Boolean
}
