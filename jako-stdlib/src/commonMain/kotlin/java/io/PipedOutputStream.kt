@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class PipedOutputStream : OutputStream {
    constructor(snk: PipedInputStream)
    constructor()
    open fun connect(snk: PipedInputStream)
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun flush()
    override fun close()
}
