@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class PipedWriter : Writer {
    constructor(snk: PipedReader)
    constructor()
    open fun connect(snk: PipedReader)
    override fun write(c: Int)
    override fun write(cbuf: CharArray, off: Int, len: Int)
    override fun flush()
    override fun close()
}
