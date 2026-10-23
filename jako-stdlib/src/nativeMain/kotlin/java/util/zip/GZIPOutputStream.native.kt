package java.util.zip

import java.io.OutputStream

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
