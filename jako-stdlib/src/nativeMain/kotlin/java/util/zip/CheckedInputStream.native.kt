package java.util.zip

import java.io.FilterInputStream
import java.io.InputStream

actual open class CheckedInputStream actual constructor(
    inStream: InputStream,
    private val cksum: Checksum
) : FilterInputStream(inStream) {

    actual override fun read(): Int {
        val b = inStream.read()
        if (b != -1) {
            cksum.update(b)
        }
        return b
    }

    actual override fun read(buf: ByteArray, off: Int, len: Int): Int {
        val n = inStream.read(buf, off, len)
        if (n != -1) {
            cksum.update(buf, off, n)
        }
        return n
    }

    actual override fun skip(n: Long): Long {
        val buf = ByteArray(512)
        var total = 0L
        while (total < n) {
            var len = n - total
            len = read(buf, 0, if (len < buf.size) len.toInt() else buf.size).toLong()
            if (len == -1L) {
                return total
            }
            total += len
        }
        return total
    }

    actual open fun getChecksum(): Checksum = cksum

    actual open val checksum: Checksum
        get() = cksum
}
