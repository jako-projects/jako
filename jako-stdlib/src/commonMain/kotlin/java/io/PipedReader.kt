@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class PipedReader : Reader {
    constructor(src: PipedWriter)
    constructor(src: PipedWriter, pipeSize: Int)
    constructor()
    constructor(pipeSize: Int)
    open fun connect(src: PipedWriter)
    override fun read(): Int
    override fun read(cbuf: CharArray, off: Int, len: Int): Int
    override fun ready(): Boolean
    override fun close()
}
