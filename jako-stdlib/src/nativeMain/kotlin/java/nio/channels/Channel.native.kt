package java.nio.channels

import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ReadOnlyBufferException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.NoSuchFileException
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption

actual interface Channel : Closeable {
    actual fun isOpen(): Boolean
    actual override fun close()
    actual val isOpen: Boolean
}

actual interface ReadableByteChannel : Channel {
    actual fun read(dst: ByteBuffer): Int
}

actual interface WritableByteChannel : Channel {
    actual fun write(src: ByteBuffer): Int
}

actual abstract class FileChannel protected actual constructor() : ReadableByteChannel, WritableByteChannel {
    actual abstract override fun read(dst: ByteBuffer): Int
    actual abstract override fun write(src: ByteBuffer): Int
    actual abstract fun position(): Long
    actual abstract fun position(newPosition: Long): FileChannel
    actual abstract fun size(): Long
    actual abstract override fun close()
    actual abstract override fun isOpen(): Boolean

    actual override val isOpen: Boolean get() = isOpen()
    actual val size: Long get() = size()

    actual companion object {
        actual fun open(path: Path, vararg options: OpenOption): FileChannel {
            var read = false
            var write = false
            var append = false
            var create = false
            var createNew = false
            var truncate = false
            var deleteOnClose = false
            for (opt in options) {
                when (opt) {
                    StandardOpenOption.READ -> read = true
                    StandardOpenOption.WRITE -> write = true
                    StandardOpenOption.APPEND -> append = true
                    StandardOpenOption.CREATE -> create = true
                    StandardOpenOption.CREATE_NEW -> createNew = true
                    StandardOpenOption.TRUNCATE_EXISTING -> truncate = true
                    StandardOpenOption.DELETE_ON_CLOSE -> deleteOnClose = true
                    StandardOpenOption.SPARSE, StandardOpenOption.SYNC, StandardOpenOption.DSYNC -> { }
                    else -> { }
                }
            }
            if (!read && !write) {
                if (append) write = true else read = true
            }
            if (read && append) throw IllegalArgumentException("READ + APPEND not allowed")
            if (append && truncate) throw IllegalArgumentException("APPEND + TRUNCATE_EXISTING not allowed")

            val file = path.toFile()
            val exists = file.exists()
            if (createNew) {
                if (exists) throw FileAlreadyExistsException(path.toString())
                FileOutputStream(file).close()
            } else if (write && !exists) {
                if (!create) throw NoSuchFileException(path.toString())
                FileOutputStream(file).close()
            } else if (!exists) {
                throw NoSuchFileException(path.toString())
            } else if (write && truncate) {
                FileOutputStream(file).close()
            }

            val raf = RandomAccessFile(file, if (write) "rw" else "r")
            return FileChannelImpl(raf, file, read, write, append, deleteOnClose)
        }
    }
}

actual open class ClosedChannelException actual constructor() : IOException()

actual open class NonReadableChannelException actual constructor() : IllegalStateException()

actual open class NonWritableChannelException actual constructor() : IllegalStateException()

internal class FileChannelImpl(
    private val raf: RandomAccessFile,
    private val file: File,
    private val readable: Boolean,
    private val writable: Boolean,
    private val append: Boolean,
    private val deleteOnClose: Boolean
) : FileChannel() {
    private var opened = true
    private var pos: Long = 0L

    private fun ensureOpen() {
        if (!opened) throw ClosedChannelException()
    }

    override fun read(dst: ByteBuffer): Int {
        ensureOpen()
        if (!readable) throw NonReadableChannelException()
        if (dst.isReadOnly()) throw ReadOnlyBufferException()
        if (!dst.hasRemaining()) return 0
        raf.seek(pos)
        val tmp = ByteArray(dst.remaining())
        val r = raf.read(tmp)
        if (r < 0) return -1
        dst.put(tmp, 0, r)
        pos += r
        return r
    }

    override fun write(src: ByteBuffer): Int {
        ensureOpen()
        if (!writable) throw NonWritableChannelException()
        if (!src.hasRemaining()) return 0
        if (append) {
            pos = raf.length()
        }
        raf.seek(pos)
        val n = src.remaining()
        val tmp = ByteArray(n)
        src.get(tmp)
        raf.write(tmp)
        pos += n
        return n
    }

    override fun position(): Long {
        ensureOpen()
        return pos
    }

    override fun position(newPosition: Long): FileChannel {
        ensureOpen()
        if (newPosition < 0L) throw IllegalArgumentException("Negative position: $newPosition")
        pos = newPosition
        return this
    }

    override fun size(): Long {
        ensureOpen()
        return raf.length()
    }

    override fun isOpen(): Boolean = opened

    override fun close() {
        if (!opened) return
        opened = false
        raf.close()
        if (deleteOnClose) {
            file.delete()
        }
    }
}
