package java.io

import com.bennyhuo.jako.NodeBridge
import com.bennyhuo.jako.toLatin1String

actual open class RandomAccessFile : DataOutput, DataInput, Closeable {
    private val filePath: String
    private val modeStr: String
    actual val fd: FileDescriptor
    private var closed = false
    internal var associatedChannelField: java.nio.channels.FileChannelImpl? = null

    actual constructor(file: File, mode: String) : this(file.getPath(), mode)

    actual constructor(name: String, mode: String) {
        this.filePath = name
        this.modeStr = mode
        if (mode != "r" && mode != "rw" && mode != "rws" && mode != "rwd") {
            throw IllegalArgumentException("Illegal mode \"$mode\" must be one of \"r\", \"rw\", \"rws\", or \"rwd\"")
        }
        val handle = NodeBridge.open(name, mode)
        if (handle < 0) {
            throw FileNotFoundException("Cannot open file: $name with mode $mode (${NodeBridge.lastError()})")
        }
        this.fd = FileDescriptor(handle)
    }

    internal fun checkOpenInternal(): Boolean = !closed && fd.valid()

    actual fun getFD(): FileDescriptor = fd

    actual open fun getChannel(): java.nio.channels.FileChannel {
        val existing = associatedChannelField
        if (existing != null) return existing
        val ch = java.nio.channels.FileChannelImpl(
            raf = this,
            file = File(filePath),
            readable = true,
            writable = modeStr != "r",
            append = false,
            deleteOnClose = false
        )
        associatedChannelField = ch
        return ch
    }

    actual open val channel: java.nio.channels.FileChannel
        get() = getChannel()

    private fun ensureOpen(): Int {
        if (closed || !fd.valid()) throw IOException("Stream closed")
        return fd.fd
    }

    actual open fun read(): Int {
        val handle = ensureOpen()
        val res = NodeBridge.read(handle, 1, -1.0)
        if (res.isEmpty() || res[0] == '0') return -1
        if (res[0] == 'E') throw IOException("Error reading file: ${res.substring(1)}")
        return if (res.length > 1) (res[1].code and 0xFF) else -1
    }

    actual open fun read(b: ByteArray, off: Int, len: Int): Int {
        val handle = ensureOpen()
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0
        val res = NodeBridge.read(handle, len, -1.0)
        if (res.isEmpty() || res[0] == '0') return -1
        if (res[0] == 'E') throw IOException("Error reading file: ${res.substring(1)}")
        val count = res.length - 1
        for (i in 0 until count) {
            b[off + i] = (res[i + 1].code and 0xFF).toByte()
        }
        return count
    }

    actual open fun read(b: ByteArray): Int = read(b, 0, b.size)

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
        val handle = ensureOpen()
        if (modeStr == "r") throw IOException("Bad file descriptor")
        val ch = (b and 0xFF).toChar().toString()
        if (NodeBridge.write(handle, ch, -1.0) < 1) {
            throw IOException("Error writing to file: ${NodeBridge.lastError()}")
        }
    }

    actual override fun write(b: ByteArray) {
        write(b, 0, b.size)
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        val handle = ensureOpen()
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return
        if (modeStr == "r") throw IOException("Bad file descriptor")
        val latin1 = b.toLatin1String(off, len)
        if (NodeBridge.write(handle, latin1, -1.0) < len) {
            throw IOException("Error writing to file: ${NodeBridge.lastError()}")
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
        val handle = ensureOpen()
        val pos = NodeBridge.lseek(handle, 0.0, 1).toLong()
        if (pos < 0L) throw IOException("Error obtaining file pointer")
        return pos
    }

    actual open val filePointer: Long get() = getFilePointer()

    actual open fun seek(pos: Long) {
        val handle = ensureOpen()
        if (pos < 0L) throw IOException("Negative seek offset")
        if (NodeBridge.lseek(handle, pos.toDouble(), 0) < 0.0) {
            throw IOException("Seek failed")
        }
    }

    actual open fun length(): Long {
        val handle = ensureOpen()
        val len = NodeBridge.fstatSize(handle).toLong()
        if (len < 0L) throw IOException("Failed to obtain length")
        return len
    }

    actual open fun setLength(newLength: Long) {
        val handle = ensureOpen()
        if (newLength < 0L) throw IllegalArgumentException("Negative length: $newLength")
        if (modeStr == "r") throw IOException("Bad file descriptor")
        val cur = getFilePointer()
        if (!NodeBridge.ftruncate(handle, newLength.toDouble())) {
            throw IOException("Failed to truncate file: $filePath (${NodeBridge.lastError()})")
        }
        val targetPos = if (cur > newLength) newLength else cur
        seek(targetPos)
    }

    internal fun flushToDiskImpl() {
        val handle = ensureOpen()
        NodeBridge.fsync(handle, true)
    }

    actual override fun close() {
        if (!closed) {
            closed = true
            associatedChannelField?.markClosedFromRaf()
            if (fd.valid()) {
                val handle = fd.fd
                fd.fd = -1
                NodeBridge.close(handle)
            }
        }
    }
}

internal actual var RandomAccessFile.associatedChannel: java.nio.channels.FileChannelImpl?
    get() = this.associatedChannelField
    set(value) {
        this.associatedChannelField = value
    }

internal actual fun RandomAccessFile.isOpenInternal(): Boolean = this.checkOpenInternal()

internal actual fun RandomAccessFile.flushToDisk() {
    this.flushToDiskImpl()
}

