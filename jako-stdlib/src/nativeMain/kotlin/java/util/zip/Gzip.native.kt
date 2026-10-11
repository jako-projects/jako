package java.util.zip

import java.io.ByteArrayInputStream
import java.io.EOFException
import java.io.FilterInputStream
import java.io.FilterOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.SequenceInputStream

actual open class InflaterInputStream : FilterInputStream {
    protected var inf: Inflater
    protected var buf: ByteArray
    protected var len: Int = 0
    internal var usesDefaultInflater: Boolean = false
    protected var reachEOF: Boolean = false
    private var closed: Boolean = false
    private val singleByte = ByteArray(1)

    actual constructor(inStream: InputStream) : this(inStream, Inflater(), 512) {
        usesDefaultInflater = true
    }

    actual constructor(inStream: InputStream, inf: Inflater) : this(inStream, inf, 512)

    actual constructor(inStream: InputStream, inf: Inflater, size: Int) : super(inStream) {
        if (size <= 0) throw IllegalArgumentException("buffer size <= 0")
        this.inf = inf
        this.buf = ByteArray(size)
    }

    protected fun ensureOpen() {
        if (closed) throw IOException("Stream closed")
    }

    actual override fun read(): Int {
        val n = read(singleByte, 0, 1)
        return if (n <= 0) -1 else singleByte[0].toInt() and 0xFF
    }

    actual override fun read(b: ByteArray, off: Int, len: Int): Int {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0
        try {
            while (true) {
                val n = inf.inflate(b, off, len)
                if (n > 0) return n
                if (inf.finished() || inf.needsDictionary()) {
                    reachEOF = true
                    return -1
                }
                if (inf.needsInput()) {
                    fill()
                }
            }
        } catch (e: DataFormatException) {
            throw ZipException(e.message)
        }
    }

    protected open fun fill() {
        ensureOpen()
        len = inStream.read(buf, 0, buf.size)
        if (len == -1) {
            throw EOFException("Unexpected end of ZLIB input stream")
        }
        inf.setInput(buf, 0, len)
    }

    actual override fun available(): Int {
        ensureOpen()
        return if (reachEOF) 0 else 1
    }

    actual override fun skip(n: Long): Long {
        if (n < 0) throw IllegalArgumentException("negative skip")
        val max = minOf(n, Int.MAX_VALUE.toLong()).toInt()
        val tmp = ByteArray(minOf(512, max))
        var total = 0
        while (total < max) {
            val count = read(tmp, 0, minOf(max - total, tmp.size))
            if (count < 0) break
            total += count
        }
        return total.toLong()
    }

    actual override fun close() {
        if (closed) return
        if (usesDefaultInflater) {
            inf.end()
        }
        inStream.close()
        closed = true
    }

    actual override fun markSupported(): Boolean = false

    actual override fun mark(readlimit: Int) {}

    actual override fun reset() {
        throw IOException("mark/reset not supported")
    }
}

actual open class DeflaterOutputStream : FilterOutputStream {
    protected var def: Deflater
    protected var buf: ByteArray
    protected var usesDefaultDeflater: Boolean = false
    private var closed: Boolean = false
    private val syncFlush: Boolean
    private val singleByte = ByteArray(1)

    actual constructor(outStream: OutputStream) : this(outStream, Deflater(), 512, false) {
        usesDefaultDeflater = true
    }

    actual constructor(outStream: OutputStream, def: Deflater) : this(outStream, def, 512, false)

    actual constructor(outStream: OutputStream, def: Deflater, size: Int) : this(outStream, def, size, false)

    actual constructor(
        outStream: OutputStream,
        def: Deflater,
        size: Int,
        syncFlush: Boolean
    ) : super(outStream) {
        if (size <= 0) throw IllegalArgumentException("buffer size <= 0")
        this.def = def
        this.buf = ByteArray(size)
        this.syncFlush = syncFlush
    }

    actual constructor(outStream: OutputStream, def: Deflater, syncFlush: Boolean) :
        this(outStream, def, 512, syncFlush)

    actual constructor(outStream: OutputStream, syncFlush: Boolean) : this(outStream, Deflater(), 512, syncFlush) {
        usesDefaultDeflater = true
    }

    protected fun ensureOpen() {
        if (closed) throw IOException("Stream closed")
    }

    actual override fun write(b: Int) {
        singleByte[0] = b.toByte()
        write(singleByte, 0, 1)
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return
        if (def.finished()) {
            throw IOException("write beyond end of stream")
        }
        def.setInput(b, off, len)
        while (!def.needsInput()) {
            deflate()
        }
    }

    protected open fun deflate() {
        val n = def.deflate(buf, 0, buf.size)
        if (n > 0) {
            out.write(buf, 0, n)
        }
    }

    actual open fun finish() {
        ensureOpen()
        if (!def.finished()) {
            def.finish()
            while (!def.finished()) {
                deflate()
            }
        }
    }

    actual override fun close() {
        if (closed) return
        try {
            finish()
        } finally {
            if (usesDefaultDeflater) {
                def.end()
            }
            out.close()
            closed = true
        }
    }

    actual override fun flush() {
        ensureOpen()
        if (syncFlush && !def.finished()) {
            var n: Int
            do {
                n = def.deflate(buf, 0, buf.size, Deflater.SYNC_FLUSH)
                if (n > 0) out.write(buf, 0, n)
            } while (n > 0)
        }
        out.flush()
    }
}

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

actual open class GZIPOutputStream : DeflaterOutputStream {
    protected val crc = CRC32()

    actual constructor(outStream: OutputStream) : this(outStream, 512, false)

    actual constructor(outStream: OutputStream, size: Int) : this(outStream, size, false)

    actual constructor(outStream: OutputStream, syncFlush: Boolean) : this(outStream, 512, syncFlush)

    actual constructor(outStream: OutputStream, size: Int, syncFlush: Boolean) :
        super(outStream, Deflater(Deflater.DEFAULT_COMPRESSION, true), size, syncFlush) {
        usesDefaultDeflater = true
        writeHeader()
    }

    actual override fun write(b: Int) {
        super.write(b)
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        super.write(b, off, len)
        crc.update(b, off, len)
    }

    actual override fun finish() {
        ensureOpen()
        if (!def.finished()) {
            def.finish()
            while (!def.finished()) {
                deflate()
            }
            writeTrailer()
        }
    }

    private fun writeHeader() {
        out.write(GZIPInputStream.GZIP_MAGIC and 0xFF)
        out.write((GZIPInputStream.GZIP_MAGIC ushr 8) and 0xFF)
        out.write(Deflater.DEFLATED)
        out.write(0)
        writeInt(0)
        out.write(0)
        out.write(0)
    }

    private fun writeTrailer() {
        writeInt(crc.getValue().toInt())
        writeInt(def.getBytesRead().toInt())
    }

    private fun writeInt(v: Int) {
        out.write(v and 0xFF)
        out.write((v ushr 8) and 0xFF)
        out.write((v ushr 16) and 0xFF)
        out.write((v ushr 24) and 0xFF)
    }
}
