package java.io

import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual open class RandomAccessFile : Closeable {
    private var filePtr: CPointer<FILE>? = null
    private val modeStr: String

    actual constructor(file: File, mode: String) : this(file.getPath(), mode)

    actual constructor(name: String, mode: String) {
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
        this.filePtr = fopen(name, cMode) ?: throw FileNotFoundException("Cannot open file: $name with mode $mode")
    }

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

    actual open fun write(b: Int) {
        val ptr = ensureOpen()
        if (fputc(b, ptr) == EOF) {
            throw IOException("Error writing to file")
        }
    }

    actual open fun write(b: ByteArray) {
        write(b, 0, b.size)
    }

    actual open fun write(b: ByteArray, off: Int, len: Int) {
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
        seek(newLength)
        if (cur < newLength) {
            seek(cur)
        }
    }

    actual override fun close() {
        val ptr = filePtr
        if (ptr != null) {
            fclose(ptr)
            filePtr = null
        }
    }
}
