@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

import java.io.InputStream

expect open class GZIPInputStream : InflaterInputStream {
    constructor(inStream: InputStream)
    constructor(inStream: InputStream, size: Int)

    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun close()

    companion object {
        val GZIP_MAGIC: Int
    }
}
