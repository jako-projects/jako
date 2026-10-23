@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

import java.io.FilterOutputStream
import java.io.OutputStream

expect open class CheckedOutputStream : FilterOutputStream {
    constructor(outStream: OutputStream, cksum: Checksum)

    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    open fun getChecksum(): Checksum
    open val checksum: Checksum
}
