package java.util.zip

import java.io.ByteArrayInputStream
import java.io.EOFException
import java.io.InputStream
import java.io.SequenceInputStream

actual open class GZIPInputStream : InflaterInputStream {
    protected val crc = CRC32()
    private var eos = false

    actual constructor(inStream: InputStream) : this(inStream, 512)

    actual constructor(inStream: InputStream, size: Int) : super(inStream, Inflater(true), size) {
        usesDefaultInflater = true
        readHeader(inStream)
    }

    actual override fun read(): Int = super.read()

    actual override fun read(b: ByteArray, off: Int, len: Int): Int {
        ensureOpen()
        if (eos) return -1
        val n = super.read(b, off, len)
        if (n == -1) {
            if (readTrailer()) {
                eos = true
                return -1
            }
            return read(b, off, len)
        }
        crc.update(b, off, n)
        return n
    }

    actual override fun close() {
        eos = true
        super.close()
    }

    private fun readHeader(`in`: InputStream): Int {
        val headerCrc = CRC32()
        fun readUByte(): Int {
            val b = `in`.read()
            if (b < 0) throw EOFException("Unexpected end of ZLIB input stream")
            headerCrc.update(b)
            return b
        }
        fun readUShort(): Int {
            val lo = readUByte()
            val hi = readUByte()
            return (hi shl 8) or lo
        }
        if (readUShort() != GZIP_MAGIC) {
            throw ZipException("Not in GZIP format")
        }
        if (readUByte() != 8) {
            throw ZipException("Unsupported compression method")
        }
        val flg = readUByte()
        repeat(6) { readUByte() }
        if ((flg and FEXTRA) != 0) {
            val extra = readUShort()
            repeat(extra) { readUByte() }
        }
        if ((flg and FNAME) != 0) {
            while (readUByte() != 0) { }
        }
        if ((flg and FCOMMENT) != 0) {
            while (readUByte() != 0) { }
        }
        if ((flg and FHCRC) != 0) {
            val expected = headerCrc.getValue().toInt() and 0xFFFF
            if (readUShort() != expected) {
                throw ZipException("Corrupt GZIP header")
            }
        }
        return 0
    }

    private fun readTrailer(): Boolean {
        val remaining = inf.getRemaining()
        var trailerIn: InputStream = inStream
        if (remaining > 0) {
            trailerIn = SequenceInputStream(
                ByteArrayInputStream(buf, len - remaining, remaining),
                inStream
            )
        }
        val crcVal = readUInt(trailerIn)
        val isize = readUInt(trailerIn)
        if (crcVal != crc.getValue()) {
            throw ZipException("Corrupt GZIP trailer")
        }
        if (isize != (inf.getBytesWritten() and 0xFFFFFFFFL)) {
            throw ZipException("Corrupt GZIP trailer")
        }
        return true
    }

    private fun readUInt(`in`: InputStream): Long {
        val b0 = readUByte(`in`)
        val b1 = readUByte(`in`)
        val b2 = readUByte(`in`)
        val b3 = readUByte(`in`)
        return b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
    }

    private fun readUByte(`in`: InputStream): Long {
        val b = `in`.read()
        if (b < 0) throw ZipException("Corrupt GZIP trailer")
        return b.toLong() and 0xFF
    }

    actual companion object {
        actual val GZIP_MAGIC: Int = 0x8b1f
        private const val FHCRC = 2
        private const val FEXTRA = 4
        private const val FNAME = 8
        private const val FCOMMENT = 16
    }
}
