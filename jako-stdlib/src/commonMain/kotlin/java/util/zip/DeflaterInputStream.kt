@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

import java.io.FilterInputStream
import java.io.InputStream

expect open class DeflaterInputStream : FilterInputStream {
    constructor(inStream: InputStream)
    constructor(inStream: InputStream, defl: Deflater)
    constructor(inStream: InputStream, defl: Deflater, bufLen: Int)

    override fun close()
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun skip(n: Long): Long
    override fun available(): Int
    override fun markSupported(): Boolean
    override fun mark(limit: Int)
    override fun reset()
}
