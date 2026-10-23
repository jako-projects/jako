package java.util.zip

import java.io.IOException
import java.io.OutputStream
import java.lang.System

actual open class ZipOutputStream : DeflaterOutputStream {
    private var current: XEntry? = null
    private val xentries = ArrayList<XEntry>()
    private val names = HashSet<String>()
    private val crc = CRC32()
    private var written = 0L
    private var locoff = 0L
    private var method = DEFLATED
    private var finishedZip = false
    private var zipClosed = false
    private var commentBytes: ByteArray? = null

    actual constructor(outStream: OutputStream) : super(
        outStream,
        Deflater(Deflater.DEFAULT_COMPRESSION, true)
    ) {
        usesDefaultDeflater = true
    }

    actual open fun setComment(comment: String?) {
        if (comment == null) {
            commentBytes = null
            return
        }
        val bytes = comment.encodeToByteArray()
        if (bytes.size > 0xFFFF) {
            throw IllegalArgumentException("invalid ZIP comment")
        }
        commentBytes = bytes
    }

    actual open fun setMethod(method: Int) {
        if (method != DEFLATED && method != STORED) {
            throw IllegalArgumentException("invalid compression method")
        }
        this.method = method
    }

    actual open fun setLevel(level: Int) {
        def.setLevel(level)
    }

    actual open fun putNextEntry(e: ZipEntry) {
        ensureOpen()
        if (finishedZip) throw IOException("Stream has already been finished")
        if (current != null) {
            closeEntry()
        }
        if (e.time == -1L) {
            e.time = System.currentTimeMillis()
        }
        if (e.method == -1) {
            e.method = method
        }
        e.flag = GPBF_EFS
        when (e.method) {
            DEFLATED -> {
                if (e.size == -1L || e.compressedSize == -1L || e.crc == -1L) {
                    e.flag = e.flag or GPBF_DATA_DESCRIPTOR
                }
            }
            STORED -> {
                if (e.size == -1L) {
                    e.size = e.compressedSize
                } else if (e.compressedSize == -1L) {
                    e.compressedSize = e.size
                } else if (e.size != e.compressedSize) {
                    throw ZipException("STORED entry where compressed != uncompressed size")
                }
                if (e.size == -1L || e.crc == -1L) {
                    throw ZipException("STORED entry missing size, compressed size, or crc-32")
                }
            }
            else -> throw ZipException("unsupported compression method")
        }
        if (!names.add(e.name)) {
            throw ZipException("duplicate entry: ${e.name}")
        }
        current = XEntry(e, written)
        xentries.add(current!!)
        writeLOC(current!!)
    }

    actual open fun closeEntry() {
        ensureOpen()
        val x = current ?: return
        val e = x.entry
        when (e.method) {
            DEFLATED -> {
                def.finish()
                while (!def.finished()) {
                    deflate()
                }
                if ((e.flag and GPBF_DATA_DESCRIPTOR) == 0) {
                    if (e.size != def.getBytesRead()) {
                        throw ZipException("invalid entry size (expected ${e.size} but got ${def.getBytesRead()} bytes)")
                    }
                    if (e.compressedSize != def.getBytesWritten()) {
                        throw ZipException(
                            "invalid entry compressed size (expected ${e.compressedSize} but got ${def.getBytesWritten()} bytes)"
                        )
                    }
                    if (e.crc != crc.getValue()) {
                        throw ZipException("invalid entry CRC-32")
                    }
                } else {
                    e.size = def.getBytesRead()
                    e.compressedSize = def.getBytesWritten()
                    e.crc = crc.getValue()
                    writeEXT(e)
                }
                def.reset()
                written += e.compressedSize
            }
            STORED -> {
                if (e.size != written - locoff) {
                    throw ZipException("invalid entry size (expected ${e.size} but got ${written - locoff} bytes)")
                }
                if (e.crc != crc.getValue()) {
                    throw ZipException("invalid entry CRC-32")
                }
            }
            else -> throw ZipException("invalid compression method")
        }
        crc.reset()
        current = null
    }

    actual override fun write(b: Int) {
        super.write(b)
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return
        val x = current ?: throw ZipException("no current ZIP entry")
        when (x.entry.method) {
            DEFLATED -> super.write(b, off, len)
            STORED -> {
                written += len
                if (written - locoff > x.entry.size) {
                    throw ZipException("attempt to write past end of STORED entry")
                }
                out.write(b, off, len)
            }
            else -> throw ZipException("invalid compression method")
        }
        crc.update(b, off, len)
    }

    actual override fun finish() {
        ensureOpen()
        if (finishedZip) return
        if (current != null) {
            closeEntry()
        }
        val off = written
        for (x in xentries) {
            writeCEN(x)
        }
        writeEND(off, written - off)
        finishedZip = true
    }

    actual override fun close() {
        if (zipClosed) return
        super.close()
        zipClosed = true
    }

    private fun writeLOC(x: XEntry) {
        val e = x.entry
        writeInt(LOCSIG)
        writeShort(versionFor(e))
        writeShort(e.flag)
        writeShort(e.method)
        writeInt(javaTimeToDosTime(e.time).toLong() and 0xFFFFFFFFL)
        if ((e.flag and GPBF_DATA_DESCRIPTOR) != 0) {
            writeInt(0)
            writeInt(0)
            writeInt(0)
        } else {
            writeInt(e.crc)
            writeInt(e.compressedSize)
            writeInt(e.size)
        }
        val nameBytes = e.name.encodeToByteArray()
        val extra = e.extra
        writeShort(nameBytes.size)
        writeShort(extra?.size ?: 0)
        writeBytes(nameBytes)
        if (extra != null) writeBytes(extra)
        locoff = written
    }

    private fun writeEXT(e: ZipEntry) {
        writeInt(EXTSIG)
        writeInt(e.crc)
        writeInt(e.compressedSize)
        writeInt(e.size)
    }

    private fun writeCEN(x: XEntry) {
        val e = x.entry
        val nameBytes = e.name.encodeToByteArray()
        val extra = e.extra
        val comment = e.comment?.encodeToByteArray()
        writeInt(CENSIG)
        writeShort(20)
        writeShort(versionFor(e))
        writeShort(e.flag)
        writeShort(e.method)
        writeInt(javaTimeToDosTime(e.time).toLong() and 0xFFFFFFFFL)
        writeInt(e.crc)
        writeInt(e.compressedSize)
        writeInt(e.size)
        writeShort(nameBytes.size)
        writeShort(extra?.size ?: 0)
        writeShort(comment?.size ?: 0)
        writeShort(0)
        writeShort(0)
        writeInt(0)
        writeInt(x.offset)
        writeBytes(nameBytes)
        if (extra != null) writeBytes(extra)
        if (comment != null) writeBytes(comment)
    }

    private fun writeEND(off: Long, size: Long) {
        val comment = commentBytes
        writeInt(ENDSIG)
        writeShort(0)
        writeShort(0)
        writeShort(xentries.size)
        writeShort(xentries.size)
        writeInt(size)
        writeInt(off)
        writeShort(comment?.size ?: 0)
        if (comment != null) writeBytes(comment)
    }

    private fun versionFor(e: ZipEntry): Int = if (e.method == DEFLATED) 20 else 10

    private fun writeInt(v: Long) {
        val n = v.toInt()
        out.write(n and 0xFF)
        out.write((n ushr 8) and 0xFF)
        out.write((n ushr 16) and 0xFF)
        out.write((n ushr 24) and 0xFF)
        written += 4
    }

    private fun writeShort(v: Int) {
        out.write(v and 0xFF)
        out.write((v ushr 8) and 0xFF)
        written += 2
    }

    private fun writeBytes(b: ByteArray) {
        out.write(b)
        written += b.size
    }

    private class XEntry(val entry: ZipEntry, val offset: Long)

    actual companion object {
        actual val STORED: Int = 0
        actual val DEFLATED: Int = 8
    }
}
