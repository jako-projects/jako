package java.nio.channels

import kotlin.concurrent.withLock

actual abstract class FileLock protected actual constructor(
    private val channel: FileChannel,
    private val position: Long,
    private val size: Long,
    private val shared: Boolean
) : AutoCloseable {
    init {
        if (position < 0L || size < 0L || position + size < 0L) {
            throw IllegalArgumentException("Invalid lock range: position=$position, size=$size")
        }
    }

    actual fun channel(): FileChannel = channel
    actual open fun acquiredBy(): Channel = channel
    actual fun position(): Long = position
    actual fun size(): Long = size
    actual fun isShared(): Boolean = shared
    actual val isShared: Boolean get() = shared

    actual fun overlaps(position: Long, size: Long): Boolean {
        if (size <= 0L || this.size <= 0L) return false
        val otherEnd = if (Long.MAX_VALUE - position < size) Long.MAX_VALUE else position + size
        val thisEnd = if (Long.MAX_VALUE - this.position < this.size) Long.MAX_VALUE else this.position + this.size
        if (otherEnd <= this.position) return false
        if (thisEnd <= position) return false
        return true
    }

    actual abstract fun isValid(): Boolean
    actual val isValid: Boolean get() = isValid()
    actual abstract fun release()

    actual override fun close() {
        release()
    }
}
