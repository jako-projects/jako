package java.util.zip

import java.io.EOFException
import java.io.InputStream
import java.io.PushbackInputStream

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
