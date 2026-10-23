@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

import java.io.FilterInputStream
import java.io.InputStream

expect open class InflaterInputStream : FilterInputStream {
    constructor(inStream: InputStream)
    constructor(inStream: InputStream, inf: Inflater)
    constructor(inStream: InputStream, inf: Inflater, size: Int)

    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun available(): Int
    override fun skip(n: Long): Long
    override fun close()
    override fun markSupported(): Boolean
    override fun mark(readlimit: Int)
    override fun reset()
}
