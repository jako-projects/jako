@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect abstract class OutputStream : Closeable, Flushable {
    constructor()
    abstract fun write(b: Int)
    open fun write(b: ByteArray)
    open fun write(b: ByteArray, off: Int, len: Int)
    override fun flush()
    override fun close()
}
