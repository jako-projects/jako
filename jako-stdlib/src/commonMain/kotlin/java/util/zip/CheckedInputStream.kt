@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

import java.io.FilterInputStream
import java.io.InputStream

expect open class CheckedInputStream : FilterInputStream {
    constructor(inStream: InputStream, cksum: Checksum)

    override fun read(): Int
    override fun read(buf: ByteArray, off: Int, len: Int): Int
    override fun skip(n: Long): Long
    open fun getChecksum(): Checksum
    open val checksum: Checksum
}
