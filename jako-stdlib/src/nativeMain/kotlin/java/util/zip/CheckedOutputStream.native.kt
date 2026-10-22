package java.util.zip

import java.io.FilterOutputStream
import java.io.OutputStream

actual open class CheckedOutputStream actual constructor(
    outStream: OutputStream,
    private val cksum: Checksum
) : FilterOutputStream(outStream) {

    actual override fun write(b: Int) {
        out.write(b)
        cksum.update(b)
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        out.write(b, off, len)
        cksum.update(b, off, len)
    }

    actual open fun getChecksum(): Checksum = cksum

    actual open val checksum: Checksum
        get() = cksum
}
