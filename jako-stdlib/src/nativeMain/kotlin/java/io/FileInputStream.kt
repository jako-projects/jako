package java.io

import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual open class FileInputStream : InputStream {
    private var filePtr: CPointer<FILE>? = null
    actual val fd: FileDescriptor

    actual constructor(name: String) {
        val ptr = fopen(name, "rb") ?: throw FileNotFoundException("File not found: $name")
        this.filePtr = ptr
        this.fd = FileDescriptor(fileno(ptr))
    }

    actual constructor(file: File) : this(file.getPath())

    actual constructor(fd: FileDescriptor) {
        this.fd = fd
        if (fd.valid()) {
            this.filePtr = fdopen(fd.fd, "rb")
        }
    }

    actual fun getFD(): FileDescriptor = fd

    actual override fun read(): Int {
        val ptr = filePtr ?: throw IOException("Stream closed")
        if (!fd.valid()) throw IOException("Stream closed")
        val ch = fgetc(ptr)
        return if (ch == EOF) -1 else ch
    }

    actual override fun read(b: ByteArray, off: Int, len: Int): Int {
        val ptr = filePtr ?: throw IOException("Stream closed")
        if (!fd.valid()) throw IOException("Stream closed")
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

    override fun skip(n: Long): Long {
        val ptr = filePtr ?: throw IOException("Stream closed")
        if (!fd.valid()) throw IOException("Stream closed")
        if (n <= 0L) return 0L
        val cur: Long = ftell(ptr).convert()
        fseek(ptr, n.convert(), SEEK_CUR)
        val newPos: Long = ftell(ptr).convert()
        return newPos - cur
    }

    override fun available(): Int {
        val ptr = filePtr ?: return 0
        if (!fd.valid()) return 0
        val cur: Long = ftell(ptr).convert()
        fseek(ptr, 0, SEEK_END)
        val end: Long = ftell(ptr).convert()
        fseek(ptr, cur.convert(), SEEK_SET)
        val rem = end - cur
        return if (rem > Int.MAX_VALUE) Int.MAX_VALUE else rem.toInt()
    }

    actual override fun close() {
        val ptr = filePtr
        if (ptr != null) {
            filePtr = null
            fd.fd = -1
            fclose(ptr)
        }
    }
}
