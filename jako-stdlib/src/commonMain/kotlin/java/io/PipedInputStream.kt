@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class PipedInputStream : InputStream {
    constructor(src: PipedOutputStream)
    constructor(src: PipedOutputStream, pipeSize: Int)
    constructor()
    constructor(pipeSize: Int)
    open fun connect(src: PipedOutputStream)
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun available(): Int
    override fun close()
}
