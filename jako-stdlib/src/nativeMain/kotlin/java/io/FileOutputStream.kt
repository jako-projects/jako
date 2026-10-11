package java.io

import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual open class FileOutputStream : OutputStream {
    private var filePtr: CPointer<FILE>? = null
    actual val fd: FileDescriptor

    actual constructor(name: String) : this(name, false)

    actual constructor(name: String, append: Boolean) {
        val mode = if (append) "ab" else "wb"
        val ptr = fopen(name, mode) ?: throw IOException("Cannot open file for writing: $name")
        this.filePtr = ptr
        this.fd = FileDescriptor(fileno(ptr))
    }

    actual constructor(file: File) : this(file.getPath(), false)

    actual constructor(file: File, append: Boolean) : this(file.getPath(), append)

    actual constructor(fd: FileDescriptor) {
        this.fd = fd
        if (fd.valid()) {
            this.filePtr = fdopen(fd.fd, "wb")
        }
    }

    actual fun getFD(): FileDescriptor = fd

    actual override fun write(b: Int) {
        val ptr = filePtr ?: throw IOException("Stream closed")
        if (!fd.valid()) throw IOException("Stream closed")
        if (fputc(b, ptr) == EOF) {
            throw IOException("Error writing to file")
        }
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        val ptr = filePtr ?: throw IOException("Stream closed")
        if (!fd.valid()) throw IOException("Stream closed")
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

    actual override fun flush() {
        val ptr = filePtr ?: return
        if (!fd.valid()) return
        fflush(ptr)
    }

    actual override fun close() {
        val ptr = filePtr
        if (ptr != null) {
            filePtr = null
            fd.fd = -1
            fflush(ptr)
            fclose(ptr)
        }
    }
}
