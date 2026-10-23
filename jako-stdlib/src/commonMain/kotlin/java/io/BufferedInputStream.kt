@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class BufferedInputStream : FilterInputStream {
    constructor(inStream: InputStream)
    constructor(inStream: InputStream, size: Int)
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
}
