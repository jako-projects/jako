@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect abstract class Reader : java.lang.Readable, Closeable {
    protected constructor()
    override fun read(target: java.nio.CharBuffer): Int
    open fun read(): Int
    open fun read(cbuf: CharArray): Int
    abstract fun read(cbuf: CharArray, off: Int, len: Int): Int
    open fun skip(n: Long): Long
    open fun ready(): Boolean
    open fun markSupported(): Boolean
    open fun mark(readAheadLimit: Int)
    open fun reset()
    abstract override fun close()
}
