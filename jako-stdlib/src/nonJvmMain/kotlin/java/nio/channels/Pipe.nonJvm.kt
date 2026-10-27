package java.nio.channels

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ReadOnlyBufferException
import kotlin.concurrent.withLock
import java.util.concurrent.locks.ReentrantLock

actual abstract class Pipe protected actual constructor() {
    actual abstract class SourceChannel internal constructor() :
        SelectableChannel(), ReadableByteChannel, ScatteringByteChannel, InterruptibleChannel {
        actual override fun validOps(): Int = SelectionKey.OP_READ
    }

    actual abstract class SinkChannel internal constructor() :
        SelectableChannel(), WritableByteChannel, GatheringByteChannel, InterruptibleChannel {
        actual override fun validOps(): Int = SelectionKey.OP_WRITE
    }

    actual abstract fun source(): SourceChannel
    actual abstract fun sink(): SinkChannel

    actual companion object {
        actual fun open(): Pipe = PipeImpl()
    }
}

internal class PipeImpl : Pipe() {
    private val lock = ReentrantLock()
    private val notEmpty = lock.newCondition()
    private val buffer = ArrayDeque<Byte>()
    private var sourceOpen = true
    private var sinkOpen = true

    private val sourceChannel = object : SourceChannel() {
        override val isOpen: Boolean get() = lock.withLock { sourceOpen }
        override fun isOpen(): Boolean = lock.withLock { sourceOpen }

        override fun pollReadyOps(): Int = lock.withLock {
            if (sourceOpen && (buffer.isNotEmpty() || !sinkOpen)) SelectionKey.OP_READ else 0
        }

        override fun read(dst: ByteBuffer): Int {
            if (dst.isReadOnly()) throw IllegalArgumentException("Read-only buffer")
            lock.withLock {
                if (!sourceOpen) throw ClosedChannelException()
                val rem = dst.remaining()
                if (rem == 0) return 0
                if (!isBlocking()) {
                    if (buffer.isEmpty()) {
                        return if (!sinkOpen) -1 else 0
                    }
                } else {
                    while (buffer.isEmpty()) {
                        if (!sinkOpen) return -1
                        notEmpty.await()
                        if (!sourceOpen) throw ClosedChannelException()
                    }
                }
                val toRead = minOf(rem, buffer.size)
                for (i in 0 until toRead) {
                    dst.put(buffer.removeFirst())
                }
                return toRead
            }
        }

        override fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long {
            if (offset < 0 || length < 0 || offset > dsts.size - length) throw IndexOutOfBoundsException()
            var total = 0L
            for (i in offset until offset + length) {
                val dst = dsts[i]
                if (dst.isReadOnly()) throw IllegalArgumentException("Read-only buffer")
                if (!dst.hasRemaining()) continue
                val n = read(dst)
                if (n < 0) return if (total == 0L) -1L else total
                if (n == 0) break
                total += n
                if (dst.hasRemaining()) break
            }
            return total
        }

        override fun read(dsts: Array<ByteBuffer>): Long = read(dsts, 0, dsts.size)

        override fun close() {
            lock.withLock {
                sourceOpen = false
                notEmpty.signalAll()
            }
            closeSelectableChannel()
        }
    }

    private val sinkChannel = object : SinkChannel() {
        override val isOpen: Boolean get() = lock.withLock { sinkOpen }
        override fun isOpen(): Boolean = lock.withLock { sinkOpen }

        override fun pollReadyOps(): Int = lock.withLock {
            if (sinkOpen && sourceOpen) SelectionKey.OP_WRITE else 0
        }

        override fun write(src: ByteBuffer): Int {
            val written = lock.withLock {
                if (!sinkOpen) throw ClosedChannelException()
                if (!sourceOpen) throw IOException("Broken pipe")
                val rem = src.remaining()
                if (rem == 0) return 0
                for (i in 0 until rem) {
                    buffer.addLast(src.get())
                }
                notEmpty.signalAll()
                rem
            }
            sourceChannel.notifySelectors()
            return written
        }

        override fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long {
            if (offset < 0 || length < 0 || offset > srcs.size - length) throw IndexOutOfBoundsException()
            var total = 0L
            for (i in offset until offset + length) {
                val src = srcs[i]
                if (!src.hasRemaining()) continue
                total += write(src)
            }
            return total
        }

        override fun write(srcs: Array<ByteBuffer>): Long = write(srcs, 0, srcs.size)

        override fun close() {
            lock.withLock {
                sinkOpen = false
                notEmpty.signalAll()
            }
            closeSelectableChannel()
            sourceChannel.notifySelectors()
        }
    }

    override fun source(): SourceChannel = sourceChannel
    override fun sink(): SinkChannel = sinkChannel
}
