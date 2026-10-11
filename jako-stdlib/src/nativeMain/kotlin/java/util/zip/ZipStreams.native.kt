package java.util.zip

import java.io.Closeable
import java.io.EOFException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PushbackInputStream
import java.io.RandomAccessFile
import java.lang.System
import java.util.Enumeration
import java.util.NoSuchElementException

actual open class ZipInputStream : InflaterInputStream {
    private val crc = CRC32()
    private var current: ZipEntry? = null
    private var remaining = 0L
    private var entryEOF = true
    private val tmpbuf = ByteArray(512)

    actual constructor(inStream: InputStream) : super(
        PushbackInputStream(inStream, 512),
        Inflater(true),
        512
    ) {
        usesDefaultInflater = true
    }

    actual open fun getNextEntry(): ZipEntry? {
        ensureOpen()
        if (current != null) {
            closeEntry()
        }
        crc.reset()
        inf.reset()
        reachEOF = false
        remaining = 0
        entryEOF = false
        current = readLOC()
        if (current == null) {
            entryEOF = true
            return null
        }
        if (current!!.method == ZipEntry.STORED) {
            remaining = current!!.size
        }
        return current
    }

    actual open fun closeEntry() {
        ensureOpen()
        while (read(tmpbuf, 0, tmpbuf.size) != -1) {
        }
        current = null
        entryEOF = true
    }

    actual override fun available(): Int {
        ensureOpen()
        return if (entryEOF) 0 else 1
    }

    actual override fun read(): Int = super.read()

    actual override fun read(b: ByteArray, off: Int, len: Int): Int {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0
        val e = current ?: return -1
        if (entryEOF) return -1
        return when (e.method) {
            ZipEntry.DEFLATED -> {
                val n = super.read(b, off, len)
                if (n == -1) {
                    readEnd(e)
                    entryEOF = true
                } else {
                    crc.update(b, off, n)
                }
                n
            }
            ZipEntry.STORED -> {
                if (remaining <= 0) {
                    if ((e.flag and GPBF_DATA_DESCRIPTOR) != 0) {
                        readDataDescriptor(e)
                    } else if (e.crc != crc.getValue()) {
                        throw ZipException("invalid entry CRC-32")
                    }
                    entryEOF = true
                    -1
                } else {
                    val toRead = minOf(len.toLong(), remaining).toInt()
                    val n = inStream.read(b, off, toRead)
                    if (n == -1) throw ZipException("unexpected EOF")
                    crc.update(b, off, n)
                    remaining -= n
                    if (remaining == 0L && (e.flag and GPBF_DATA_DESCRIPTOR) == 0) {
                        if (e.crc != crc.getValue()) {
                            throw ZipException("invalid entry CRC-32")
                        }
                    }
                    n
                }
            }
            else -> throw ZipException("invalid compression method")
        }
    }

    actual override fun close() {
        current = null
        super.close()
    }

    protected actual open fun createZipEntry(name: String): ZipEntry = ZipEntry(name)

    private fun readLOC(): ZipEntry? {
        try {
            inStream.readFully(tmpbuf, 0, LOCHDR)
        } catch (_: EOFException) {
            return null
        }
        val sig = get32(tmpbuf, 0)
        if (sig == CENSIG || sig == ENDSIG) {
            return null
        }
        if (sig != LOCSIG) {
            throw ZipException("invalid LOC header (bad signature)")
        }
        val flag = get16(tmpbuf, 6)
        val method = get16(tmpbuf, 8)
        val dostime = get32(tmpbuf, 10).toInt()
        val crcVal = get32(tmpbuf, 14)
        val csize = get32(tmpbuf, 18)
        val size = get32(tmpbuf, 22)
        val nameLen = get16(tmpbuf, 26)
        val extraLen = get16(tmpbuf, 28)
        val nameBytes = ByteArray(nameLen)
        inStream.readFully(nameBytes, 0, nameLen)
        val e = createZipEntry(nameBytes.decodeToString())
        e.flag = flag
        e.method = method
        e.time = dosTimeToJavaTime(dostime)
        if ((flag and GPBF_DATA_DESCRIPTOR) == 0) {
            e.crc = crcVal
            e.compressedSize = csize
            e.size = size
        }
        if (extraLen > 0) {
            val extra = ByteArray(extraLen)
            inStream.readFully(extra, 0, extraLen)
            e.extra = extra
        }
        return e
    }

    private fun readEnd(e: ZipEntry) {
        val leftover = inf.getRemaining()
        if (leftover > 0) {
            val extra = buf.copyOfRange(len - leftover, len)
            (inStream as PushbackInputStream).unread(extra)
        }
        if ((e.flag and GPBF_DATA_DESCRIPTOR) != 0) {
            readDataDescriptor(e)
        }
        if (e.size != inf.getBytesWritten()) {
            throw ZipException("invalid entry size (expected ${e.size} but got ${inf.getBytesWritten()} bytes)")
        }
        if (e.compressedSize != inf.getBytesRead()) {
            throw ZipException(
                "invalid entry compressed size (expected ${e.compressedSize} but got ${inf.getBytesRead()} bytes)"
            )
        }
        if (e.crc != crc.getValue()) {
            throw ZipException("invalid entry CRC-32")
        }
    }

    private fun readDataDescriptor(e: ZipEntry) {
        inStream.readFully(tmpbuf, 0, 4)
        val first = get32(tmpbuf, 0)
        if (first == EXTSIG) {
            inStream.readFully(tmpbuf, 0, 12)
            e.crc = get32(tmpbuf, 0)
            e.compressedSize = get32(tmpbuf, 4)
            e.size = get32(tmpbuf, 8)
        } else {
            e.crc = first
            inStream.readFully(tmpbuf, 0, 8)
            e.compressedSize = get32(tmpbuf, 0)
            e.size = get32(tmpbuf, 4)
        }
    }
}

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

actual open class ZipFile : Closeable {
    private val file: File
    private val raf: RandomAccessFile
    private val deleteOnClose: Boolean
    private val fileName: String
    private val entryMap = LinkedHashMap<String, CenRec>()
    private var zipComment: String? = null
    private var closed = false

    actual constructor(name: String) : this(File(name), OPEN_READ)

    actual constructor(file: File) : this(file, OPEN_READ)

    actual constructor(file: File, mode: Int) {
        if ((mode and OPEN_READ) == 0 || (mode and (OPEN_READ or OPEN_DELETE).inv()) != 0) {
            throw IllegalArgumentException("Illegal mode: $mode")
        }
        this.file = file
        this.fileName = file.getPath()
        this.deleteOnClose = (mode and OPEN_DELETE) != 0
        this.raf = RandomAccessFile(file, "r")
        try {
            readCEN()
        } catch (e: Exception) {
            raf.close()
            throw e
        }
    }

    actual open fun getComment(): String? {
        ensureOpen()
        return zipComment
    }

    actual open val comment: String? get() = getComment()

    actual open fun getEntry(name: String): ZipEntry? {
        ensureOpen()
        val rec = entryMap[name] ?: return null
        return ZipEntry(rec.entry)
    }

    actual open fun getInputStream(entry: ZipEntry): InputStream? {
        ensureOpen()
        val rec = entryMap[entry.name] ?: return null
        val dataOff = localDataOffset(rec.offset)
        val stored = ZipFileStoredStream(dataOff, rec.csize)
        return when (rec.method) {
            ZipEntry.STORED -> stored
            ZipEntry.DEFLATED -> InflaterInputStream(stored, Inflater(true), 512).also {
                it.usesDefaultInflater = true
            }
            else -> throw ZipException("unsupported compression method")
        }
    }

    actual open fun getName(): String = fileName
    actual open val name: String get() = getName()

    actual open fun entries(): Enumeration<out ZipEntry> {
        ensureOpen()
        val list = entryMap.values.map { ZipEntry(it.entry) }
        var i = 0
        return object : Enumeration<ZipEntry> {
            override fun hasMoreElements(): Boolean = i < list.size
            override fun nextElement(): ZipEntry {
                if (i >= list.size) throw NoSuchElementException()
                return list[i++]
            }
        }
    }

    actual open fun size(): Int {
        ensureOpen()
        return entryMap.size
    }

    actual override fun close() {
        if (closed) return
        closed = true
        raf.close()
        if (deleteOnClose) {
            file.delete()
        }
    }

    private fun ensureOpen() {
        if (closed) throw IllegalStateException("zip file closed")
    }

    private fun readCEN() {
        val eocdOff = findEOCD()
        raf.seek(eocdOff)
        val eocd = ByteArray(ENDHDR)
        raf.readFully(eocd)
        val totalEntries = get16(eocd, 10)
        val cenSize = get32(eocd, 12)
        val cenOff = get32(eocd, 16)
        val commentLen = get16(eocd, 20)
        if (commentLen > 0) {
            val cb = ByteArray(commentLen)
            raf.readFully(cb)
            zipComment = cb.decodeToString()
        }
        if (cenSize > Int.MAX_VALUE || totalEntries < 0) {
            throw ZipException("invalid END header")
        }
        raf.seek(cenOff)
        val cen = ByteArray(cenSize.toInt())
        raf.readFully(cen)
        var p = 0
        repeat(totalEntries) {
            if (p + CENHDR > cen.size || get32(cen, p) != CENSIG) {
                throw ZipException("invalid CEN header (bad signature)")
            }
            val flag = get16(cen, p + 8)
            val method = get16(cen, p + 10)
            val dostime = get32(cen, p + 12).toInt()
            val crc = get32(cen, p + 16)
            val csize = get32(cen, p + 20)
            val size = get32(cen, p + 24)
            val nameLen = get16(cen, p + 28)
            val extraLen = get16(cen, p + 30)
            val commentLenE = get16(cen, p + 32)
            val offset = get32(cen, p + 42)
            if (csize == 0xFFFFFFFFL || size == 0xFFFFFFFFL || offset == 0xFFFFFFFFL) {
                throw ZipException("ZIP64 is not supported")
            }
            val nameStart = p + CENHDR
            val nameEnd = nameStart + nameLen
            if (nameEnd > cen.size) throw ZipException("invalid CEN header (bad name length)")
            val name = cen.decodeToString(nameStart, nameEnd)
            val extra = if (extraLen > 0) {
                val extraEnd = nameEnd + extraLen
                if (extraEnd > cen.size) throw ZipException("invalid CEN header (bad extra length)")
                cen.copyOfRange(nameEnd, extraEnd)
            } else {
                null
            }
            val comment = if (commentLenE > 0) {
                val cStart = nameEnd + extraLen
                val cEnd = cStart + commentLenE
                if (cEnd > cen.size) throw ZipException("invalid CEN header (bad comment length)")
                cen.decodeToString(cStart, cEnd)
            } else {
                null
            }
            val entry = ZipEntry(name)
            entry.method = method
            entry.time = dosTimeToJavaTime(dostime)
            entry.crc = crc
            entry.compressedSize = csize
            entry.size = size
            entry.extra = extra
            entry.comment = comment
            entry.flag = flag
            entry.offset = offset
            entryMap[name] = CenRec(entry, offset, method, csize, size, flag)
            p = nameEnd + extraLen + commentLenE
        }
    }

    private fun findEOCD(): Long {
        val length = raf.length()
        if (length < ENDHDR) throw ZipException("zip file empty or too small")
        val scanLen = minOf(length, ENDHDR.toLong() + 65535L)
        val start = length - scanLen
        val buf = ByteArray(scanLen.toInt())
        raf.seek(start)
        raf.readFully(buf)
        var i = buf.size - ENDHDR
        while (i >= 0) {
            if (buf[i] == 0x50.toByte() && buf[i + 1] == 0x4b.toByte() &&
                buf[i + 2] == 5.toByte() && buf[i + 3] == 6.toByte()
            ) {
                val commentLen = get16(buf, i + 20)
                if (i.toLong() + ENDHDR + commentLen == buf.size.toLong()) {
                    return start + i
                }
            }
            i--
        }
        throw ZipException("zip END header not found")
    }

    private fun localDataOffset(locoff: Long): Long {
        synchronized(this) {
            raf.seek(locoff)
            val loc = ByteArray(LOCHDR)
            raf.readFully(loc)
            if (get32(loc, 0) != LOCSIG) {
                throw ZipException("invalid LOC header (bad signature)")
            }
            val nameLen = get16(loc, 26)
            val extraLen = get16(loc, 28)
            return locoff + LOCHDR + nameLen + extraLen
        }
    }

    private inner class ZipFileStoredStream(
        private var pos: Long,
        private var remaining: Long
    ) : InputStream() {
        override fun read(): Int {
            val b = ByteArray(1)
            val n = read(b, 0, 1)
            return if (n <= 0) -1 else b[0].toInt() and 0xFF
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (off < 0 || len < 0 || off > b.size - len) {
                throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
            }
            if (len == 0) return 0
            if (remaining <= 0) return -1
            val n = minOf(len.toLong(), remaining).toInt()
            synchronized(this@ZipFile) {
                ensureOpen()
                raf.seek(pos)
                val r = raf.read(b, off, n)
                if (r > 0) {
                    pos += r
                    remaining -= r
                }
                return r
            }
        }
    }

    private class CenRec(
        val entry: ZipEntry,
        val offset: Long,
        val method: Int,
        val csize: Long,
        val size: Long,
        val flag: Int
    )

    actual companion object {
        actual val OPEN_READ: Int = 1
        actual val OPEN_DELETE: Int = 4
    }
}
