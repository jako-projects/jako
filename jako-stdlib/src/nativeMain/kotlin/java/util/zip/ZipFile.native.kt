package java.util.zip

import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.Enumeration
import java.util.NoSuchElementException

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
