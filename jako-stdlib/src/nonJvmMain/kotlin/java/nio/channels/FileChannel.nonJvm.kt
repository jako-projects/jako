package java.nio.channels

import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.io.associatedChannel
import java.io.flushToDisk
import java.io.isOpenInternal
import java.nio.ByteBuffer
import java.nio.ReadOnlyBufferException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.NoSuchFileException
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.concurrent.withLock
import java.util.concurrent.locks.ReentrantLock

actual abstract class FileChannel protected actual constructor() :
    SeekableByteChannel, GatheringByteChannel, ScatteringByteChannel, ByteChannel, InterruptibleChannel {

    actual abstract override fun read(dst: ByteBuffer): Int
    actual abstract override fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long
    actual override fun read(dsts: Array<ByteBuffer>): Long = read(dsts, 0, dsts.size)
    actual abstract fun read(dst: ByteBuffer, position: Long): Int

    actual abstract override fun write(src: ByteBuffer): Int
    actual abstract override fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long
    actual override fun write(srcs: Array<ByteBuffer>): Long = write(srcs, 0, srcs.size)
    actual abstract fun write(src: ByteBuffer, position: Long): Int

    actual abstract override fun position(): Long
    actual abstract override fun position(newPosition: Long): FileChannel
    actual abstract override fun size(): Long
    actual abstract override fun truncate(size: Long): FileChannel
    actual abstract fun force(metaData: Boolean)
    actual abstract fun transferTo(position: Long, count: Long, target: WritableByteChannel): Long
    actual abstract fun transferFrom(src: ReadableByteChannel, position: Long, count: Long): Long
    actual abstract fun lock(position: Long, size: Long, shared: Boolean): FileLock
    actual fun lock(): FileLock = lock(0L, Long.MAX_VALUE, false)
    actual abstract fun tryLock(position: Long, size: Long, shared: Boolean): FileLock?
    actual fun tryLock(): FileLock? = tryLock(0L, Long.MAX_VALUE, false)
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
            val channel = FileChannelImpl(raf, file, read, write, append, deleteOnClose)
            raf.associatedChannel = channel
            return channel
        }
    }
}

internal object ActiveFileLockRegistry {
    private val lock = ReentrantLock()
    private val locksByPath = HashMap<String, MutableList<FileLockImpl>>()

    fun acquire(
        fileKey: String,
        channel: FileChannelImpl,
        position: Long,
        size: Long,
        shared: Boolean
    ): FileLockImpl {
        return lock.withLock {
            val list = locksByPath.getOrPut(fileKey) { ArrayList() }
            for (existing in list) {
                if (existing.isValid() && existing.overlaps(position, size)) {
                    throw OverlappingFileLockException()
                }
            }
            val fileLock = FileLockImpl(fileKey, channel, position, size, shared)
            list.add(fileLock)
            fileLock
        }
    }

    fun release(fileLock: FileLockImpl) {
        lock.withLock {
            fileLock.markInvalid()
            val list = locksByPath[fileLock.fileKey] ?: return@withLock
            list.remove(fileLock)
            if (list.isEmpty()) {
                locksByPath.remove(fileLock.fileKey)
            }
        }
    }

    fun releaseAllForChannel(channel: FileChannelImpl) {
        lock.withLock {
            val iter = locksByPath.entries.iterator()
            while (iter.hasNext()) {
                val entry = iter.next()
                val list = entry.value
                val lockIter = list.iterator()
                while (lockIter.hasNext()) {
                    val fl = lockIter.next()
                    if (fl.channel() === channel) {
                        fl.markInvalid()
                        lockIter.remove()
                    }
                }
                if (list.isEmpty()) {
                    iter.remove()
                }
            }
        }
    }
}

internal class FileLockImpl(
    internal val fileKey: String,
    private val ownerChannel: FileChannelImpl,
    position: Long,
    size: Long,
    shared: Boolean
) : FileLock(ownerChannel, position, size, shared) {
    private var valid = true

    internal fun markInvalid() {
        valid = false
    }

    override fun isValid(): Boolean = valid && ownerChannel.isOpen()

    override fun release() {
        if (!ownerChannel.isOpen()) throw ClosedChannelException()
        if (valid) {
            ActiveFileLockRegistry.release(this)
        }
    }
}

internal class FileChannelImpl(
    private val raf: RandomAccessFile,
    private val file: File,
    internal val readable: Boolean,
    internal val writable: Boolean,
    private val append: Boolean,
    private val deleteOnClose: Boolean
) : FileChannel() {
    private var opened = true
    private val ioLock = ReentrantLock()
    private val canonicalKey: String = try {
        file.getCanonicalPath()
    } catch (_: Throwable) {
        file.getAbsolutePath()
    }

    private fun ensureOpen() {
        if (!opened || !raf.isOpenInternal()) throw ClosedChannelException()
    }

    override fun read(dst: ByteBuffer): Int = ioLock.withLock {
        ensureOpen()
        if (!readable) throw NonReadableChannelException()
        if (dst.isReadOnly()) throw ReadOnlyBufferException()
        if (!dst.hasRemaining()) return 0
        val curPos = raf.getFilePointer()
        val curSize = raf.length()
        if (curPos >= curSize) return -1
        val pos = dst.position()
        val r = raf.read(dst.arrayData, dst.offset + pos, dst.remaining())
        if (r < 0) return -1
        dst.position(pos + r)
        return r
    }

    override fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long = ioLock.withLock {
        if (offset < 0 || length < 0 || offset > dsts.size - length) {
            throw IndexOutOfBoundsException("offset=$offset, length=$length, dsts.size=${dsts.size}")
        }
        ensureOpen()
        if (!readable) throw NonReadableChannelException()
        for (i in offset until offset + length) {
            if (dsts[i].isReadOnly()) throw ReadOnlyBufferException()
        }
        var totalRead = 0L
        for (i in offset until offset + length) {
            val dst = dsts[i]
            while (dst.hasRemaining()) {
                val n = read(dst)
                if (n < 0) {
                    return if (totalRead == 0L) -1L else totalRead
                }
                if (n == 0) break
                totalRead += n
            }
        }
        return totalRead
    }

    override fun read(dst: ByteBuffer, position: Long): Int = ioLock.withLock {
        if (position < 0L) throw IllegalArgumentException("Negative position: $position")
        ensureOpen()
        if (!readable) throw NonReadableChannelException()
        if (dst.isReadOnly()) throw ReadOnlyBufferException()
        if (!dst.hasRemaining()) return 0
        val curSize = raf.length()
        if (position >= curSize) return -1
        val savedPos = raf.getFilePointer()
        try {
            raf.seek(position)
            val pos = dst.position()
            val r = raf.read(dst.arrayData, dst.offset + pos, dst.remaining())
            if (r < 0) return -1
            dst.position(pos + r)
            return r
        } finally {
            raf.seek(savedPos)
        }
    }

    override fun write(src: ByteBuffer): Int = ioLock.withLock {
        ensureOpen()
        if (!writable) throw NonWritableChannelException()
        if (!src.hasRemaining()) return 0
        if (append) {
            raf.seek(raf.length())
        } else {
            val curPos = raf.getFilePointer()
            val curLen = raf.length()
            if (curPos > curLen) {
                raf.setLength(curPos)
                raf.seek(curPos)
            }
        }
        val pos = src.position()
        val n = src.remaining()
        raf.write(src.arrayData, src.offset + pos, n)
        src.position(pos + n)
        return n
    }

    override fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long = ioLock.withLock {
        if (offset < 0 || length < 0 || offset > srcs.size - length) {
            throw IndexOutOfBoundsException("offset=$offset, length=$length, srcs.size=${srcs.size}")
        }
        ensureOpen()
        if (!writable) throw NonWritableChannelException()
        var totalWritten = 0L
        for (i in offset until offset + length) {
            val src = srcs[i]
            while (src.hasRemaining()) {
                val n = write(src)
                if (n <= 0) break
                totalWritten += n
            }
        }
        return totalWritten
    }

    override fun write(src: ByteBuffer, position: Long): Int = ioLock.withLock {
        if (position < 0L) throw IllegalArgumentException("Negative position: $position")
        ensureOpen()
        if (!writable) throw NonWritableChannelException()
        if (!src.hasRemaining()) return 0
        val savedPos = raf.getFilePointer()
        try {
            val curLen = raf.length()
            if (position > curLen) {
                raf.setLength(position)
            }
            raf.seek(position)
            val pos = src.position()
            val n = src.remaining()
            raf.write(src.arrayData, src.offset + pos, n)
            src.position(pos + n)
            return n
        } finally {
            raf.seek(savedPos)
        }
    }

    override fun position(): Long = ioLock.withLock {
        ensureOpen()
        raf.getFilePointer()
    }

    override fun position(newPosition: Long): FileChannel = ioLock.withLock {
        ensureOpen()
        if (newPosition < 0L) throw IllegalArgumentException("Negative position: $newPosition")
        raf.seek(newPosition)
        this
    }

    override fun size(): Long = ioLock.withLock {
        ensureOpen()
        raf.length()
    }

    override fun truncate(size: Long): FileChannel = ioLock.withLock {
        if (size < 0L) throw IllegalArgumentException("Negative size: $size")
        ensureOpen()
        if (!writable) throw NonWritableChannelException()
        val curPos = raf.getFilePointer()
        if (size < raf.length()) {
            raf.setLength(size)
        }
        if (curPos > size) {
            raf.seek(size)
        } else {
            raf.seek(curPos)
        }
        this
    }

    override fun force(metaData: Boolean) = ioLock.withLock {
        ensureOpen()
        raf.flushToDisk()
    }

    override fun transferTo(position: Long, count: Long, target: WritableByteChannel): Long = ioLock.withLock {
        if (position < 0L || count < 0L) throw IllegalArgumentException("position=$position, count=$count")
        ensureOpen()
        if (!target.isOpen()) throw ClosedChannelException()
        if (!readable) throw NonReadableChannelException()
        if (target is FileChannelImpl && !target.writable) throw NonWritableChannelException()
        val curSize = raf.length()
        if (position >= curSize || count == 0L) return 0L
        val toRead = minOf(count, curSize - position)
        var remaining = toRead
        var currPos = position
        var transferred = 0L
        val buf = ByteBuffer.allocate(minOf(remaining, 8192L).toInt())
        while (remaining > 0L) {
            buf.clear()
            val chunk = minOf(remaining, buf.capacity().toLong()).toInt()
            buf.limit(chunk)
            val r = read(buf, currPos)
            if (r <= 0) break
            buf.flip()
            val w = target.write(buf)
            transferred += w
            currPos += w
            remaining -= w
            if (w < r) break
        }
        return transferred
    }

    override fun transferFrom(src: ReadableByteChannel, position: Long, count: Long): Long = ioLock.withLock {
        if (position < 0L || count < 0L) throw IllegalArgumentException("position=$position, count=$count")
        ensureOpen()
        if (!src.isOpen()) throw ClosedChannelException()
        if (!writable) throw NonWritableChannelException()
        if (src is FileChannelImpl && !src.readable) throw NonReadableChannelException()
        if (position > raf.length() || count == 0L) return 0L
        var remaining = count
        var currPos = position
        var transferred = 0L
        val buf = ByteBuffer.allocate(minOf(remaining, 8192L).toInt())
        while (remaining > 0L) {
            buf.clear()
            val chunk = minOf(remaining, buf.capacity().toLong()).toInt()
            buf.limit(chunk)
            val r = src.read(buf)
            if (r <= 0) break
            buf.flip()
            val w = write(buf, currPos)
            transferred += w
            currPos += w
            remaining -= w
        }
        return transferred
    }

    override fun lock(position: Long, size: Long, shared: Boolean): FileLock {
        if (position < 0L || size < 0L || position + size < 0L) {
            throw IllegalArgumentException("Invalid lock range: position=$position, size=$size")
        }
        ensureOpen()
        if (shared && !readable) throw NonReadableChannelException()
        if (!shared && !writable) throw NonWritableChannelException()
        return ActiveFileLockRegistry.acquire(canonicalKey, this, position, size, shared)
    }

    override fun tryLock(position: Long, size: Long, shared: Boolean): FileLock? {
        return lock(position, size, shared)
    }

    override fun isOpen(): Boolean = opened && raf.isOpenInternal()

    internal fun markClosedFromRaf() {
        if (!opened) return
        opened = false
        ActiveFileLockRegistry.releaseAllForChannel(this)
    }

    override fun close() {
        if (!opened) return
        opened = false
        ActiveFileLockRegistry.releaseAllForChannel(this)
        raf.close()
        if (deleteOnClose) {
            file.delete()
        }
    }
}
