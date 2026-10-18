package java.io

import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual open class RandomAccessFile : DataOutput, DataInput, Closeable {
    private var filePtr: CPointer<FILE>? = null
    private val filePath: String
    private val modeStr: String
    actual val fd: FileDescriptor
    internal var associatedChannel: java.nio.channels.FileChannelImpl? = null

    actual constructor(file: File, mode: String) : this(file.getPath(), mode)

    actual constructor(name: String, mode: String) {
        this.filePath = name
        this.modeStr = mode
        val cMode = when (mode) {
            "r" -> "rb"
            "rw", "rws", "rwd" -> {
                val checkFile = fopen(name, "rb")
                if (checkFile != null) {
                    fclose(checkFile)
                    "r+b"
                } else {
                    "w+b"
                }
            }
            else -> throw IllegalArgumentException("Illegal mode \"$mode\" must be one of \"r\", \"rw\", \"rws\", or \"rwd\"")
        }
        val ptr = fopen(name, cMode) ?: throw FileNotFoundException("Cannot open file: $name with mode $mode")
        this.filePtr = ptr
        this.fd = FileDescriptor(fileno(ptr))
    }

    internal fun isOpenInternal(): Boolean = filePtr != null

    actual fun getFD(): FileDescriptor = fd

    actual open fun getChannel(): java.nio.channels.FileChannel {
        val existing = associatedChannel
        if (existing != null) return existing
        val ch = java.nio.channels.FileChannelImpl(
            raf = this,
            file = File(filePath),
            readable = true,
            writable = modeStr != "r",
            append = false,
            deleteOnClose = false
        )
        associatedChannel = ch
        return ch
    }

    actual open val channel: java.nio.channels.FileChannel
        get() = getChannel()

    private fun ensureOpen(): CPointer<FILE> {
        return filePtr ?: throw IOException("Stream closed")
    }

    actual open fun read(): Int {
        val ptr = ensureOpen()
        val ch = fgetc(ptr)
        return if (ch == EOF) -1 else ch
    }

    actual open fun read(b: ByteArray, off: Int, len: Int): Int {
        val ptr = ensureOpen()
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0

        b.usePinned { pinned ->
            val readCount: Int = fread(pinned.addressOf(off), 1.convert(), len.convert(), ptr).convert()
            return if (readCount == 0) {
                if (feof(ptr) != 0) -1 else throw IOException("Error reading file")
            } else {
                readCount
            }
        }
    }

    actual open fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    actual override fun readFully(b: ByteArray) {
        readFully(b, 0, b.size)
    }

    actual override fun readFully(b: ByteArray, off: Int, len: Int) {
        if (len < 0) throw IndexOutOfBoundsException()
        var n = 0
        while (n < len) {
            val count = read(b, off + n, len - n)
            if (count < 0) throw EOFException()
            n += count
        }
    }

    actual override fun skipBytes(n: Int): Int {
        if (n <= 0) return 0
        val pos = getFilePointer()
        val len = length()
        var newpos = pos + n
        if (newpos > len) {
            newpos = len
        }
        seek(newpos)
        return (newpos - pos).toInt()
    }

    actual override fun write(b: Int) {
        val ptr = ensureOpen()
        if (fputc(b, ptr) == EOF) {
            throw IOException("Error writing to file")
        }
    }

    actual override fun write(b: ByteArray) {
        write(b, 0, b.size)
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        val ptr = ensureOpen()
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return

        b.usePinned { pinned ->
            val written: Int = fwrite(pinned.addressOf(off), 1.convert(), len.convert(), ptr).convert()
            if (written < len) {
                throw IOException("Error writing to file")
            }
        }
    }

    actual override fun readBoolean(): Boolean {
        val ch = read()
        if (ch < 0) throw EOFException()
        return ch != 0
    }

    actual override fun readByte(): Byte {
        val ch = read()
        if (ch < 0) throw EOFException()
        return ch.toByte()
    }

    actual override fun readUnsignedByte(): Int {
        val ch = read()
        if (ch < 0) throw EOFException()
        return ch
    }

    actual override fun readShort(): Short {
        val ch1 = read()
        val ch2 = read()
        if ((ch1 or ch2) < 0) throw EOFException()
        return ((ch1 shl 8) + (ch2 shl 0)).toShort()
    }

    actual override fun readUnsignedShort(): Int {
        val ch1 = read()
        val ch2 = read()
        if ((ch1 or ch2) < 0) throw EOFException()
        return (ch1 shl 8) + (ch2 shl 0)
    }

    actual override fun readChar(): Char {
        val ch1 = read()
        val ch2 = read()
        if ((ch1 or ch2) < 0) throw EOFException()
        return ((ch1 shl 8) + (ch2 shl 0)).toChar()
    }

    actual override fun readInt(): Int {
        val ch1 = read()
        val ch2 = read()
        val ch3 = read()
        val ch4 = read()
        if ((ch1 or ch2 or ch3 or ch4) < 0) throw EOFException()
        return (ch1 shl 24) or (ch2 shl 16) or (ch3 shl 8) or ch4
    }

    actual override fun readLong(): Long {
        return (readInt().toLong() shl 32) + (readInt().toLong() and 0xFFFFFFFFL)
    }

    actual override fun readFloat(): Float = Float.fromBits(readInt())

    actual override fun readDouble(): Double = Double.fromBits(readLong())

    actual override fun readLine(): String? {
        val input = StringBuilder()
        var c = -1
        var eol = false
        while (!eol) {
            c = read()
            when (c) {
                -1, '\n'.code -> eol = true
                '\r'.code -> {
                    eol = true
                    val cur = getFilePointer()
                    if (read() != '\n'.code) {
                        seek(cur)
                    }
                }
                else -> input.append((c and 0xFF).toChar())
            }
        }
        if (c == -1 && input.isEmpty()) {
            return null
        }
        return input.toString()
    }

    actual override fun readUTF(): String {
        val utflen = readUnsignedShort()
        val bytearr = ByteArray(utflen)
        readFully(bytearr, 0, utflen)
        return bytearr.decodeToString()
    }

    actual override fun writeBoolean(v: Boolean) {
        write(if (v) 1 else 0)
    }

    actual override fun writeByte(v: Int) {
        write(v)
    }

    actual override fun writeShort(v: Int) {
        write((v ushr 8) and 0xFF)
        write((v ushr 0) and 0xFF)
    }

    actual override fun writeChar(v: Int) {
        write((v ushr 8) and 0xFF)
        write((v ushr 0) and 0xFF)
    }

    actual override fun writeInt(v: Int) {
        write((v ushr 24) and 0xFF)
        write((v ushr 16) and 0xFF)
        write((v ushr 8) and 0xFF)
        write((v ushr 0) and 0xFF)
    }

    actual override fun writeLong(v: Long) {
        write((v ushr 56).toInt() and 0xFF)
        write((v ushr 48).toInt() and 0xFF)
        write((v ushr 40).toInt() and 0xFF)
        write((v ushr 32).toInt() and 0xFF)
        write((v ushr 24).toInt() and 0xFF)
        write((v ushr 16).toInt() and 0xFF)
        write((v ushr 8).toInt() and 0xFF)
        write((v ushr 0).toInt() and 0xFF)
    }

    actual override fun writeFloat(v: Float) {
        writeInt(v.toRawBits())
    }

    actual override fun writeDouble(v: Double) {
        writeLong(v.toRawBits())
    }

    actual override fun writeBytes(s: String) {
        for (i in s.indices) {
            write(s[i].code and 0xFF)
        }
    }

    actual override fun writeChars(s: String) {
        for (i in s.indices) {
            writeChar(s[i].code)
        }
    }

    actual override fun writeUTF(s: String) {
        val bytes = s.encodeToByteArray()
        if (bytes.size > 65535) {
            throw IOException("encoded string too long: ${bytes.size} bytes")
        }
        writeShort(bytes.size)
        write(bytes, 0, bytes.size)
    }

    actual open fun getFilePointer(): Long {
        val ptr = ensureOpen()
        val pos: Long = ftell(ptr).convert()
        if (pos < 0L) throw IOException("Error obtaining file pointer")
        return pos
    }

    actual open val filePointer: Long get() = getFilePointer()

    actual open fun seek(pos: Long) {
        val ptr = ensureOpen()
        if (pos < 0L) throw IOException("Negative seek offset")
        if (fseek(ptr, pos.convert(), SEEK_SET) != 0) {
            throw IOException("Seek failed")
        }
    }

    actual open fun length(): Long {
        val ptr = ensureOpen()
        val cur: Long = ftell(ptr).convert()
        fseek(ptr, 0, SEEK_END)
        val len: Long = ftell(ptr).convert()
        fseek(ptr, cur.convert(), SEEK_SET)
        return len
    }

    actual open fun setLength(newLength: Long) {
        val ptr = ensureOpen()
        if (newLength < 0L) throw IllegalArgumentException("Negative length: $newLength")
        val cur = getFilePointer()
        val oldLen = length()
        if (newLength < oldLen) {
            val keep = ByteArray(newLength.toInt())
            seek(0L)
            var off = 0
            while (off < keep.size) {
                val n = read(keep, off, keep.size - off)
                if (n <= 0) break
                off += n
            }
            fclose(ptr)
            val truncated = fopen(filePath, "w+b") ?: throw IOException("Failed to truncate file: $filePath")
            filePtr = truncated
            fd.fd = fileno(truncated)
            if (keep.isNotEmpty()) {
                write(keep, 0, keep.size)
            }
            fflush(truncated)
            val targetPos = if (cur > newLength) newLength else cur
            seek(targetPos)
        } else if (newLength > oldLen) {
            seek(oldLen)
            val zeros = ByteArray((newLength - oldLen).coerceAtMost(8192L).toInt())
            var rem = newLength - oldLen
            while (rem > 0L) {
                val chunk = minOf(rem, zeros.size.toLong()).toInt()
                write(zeros, 0, chunk)
                rem -= chunk
            }
            fflush(ptr)
            seek(cur)
        }
    }

    internal fun flushToDisk() {
        val ptr = ensureOpen()
        fflush(ptr)
    }

    actual override fun close() {
        val ptr = filePtr
        if (ptr != null) {
            filePtr = null
            fd.fd = -1
            associatedChannel?.markClosedFromRaf()
            fclose(ptr)
        }
    }
}
