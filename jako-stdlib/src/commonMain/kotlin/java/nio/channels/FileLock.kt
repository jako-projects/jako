@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

/**
 * [Nullability Decision] FileLock.channel() and acquiredBy() return non-null channel instances.
 */
expect abstract class FileLock : AutoCloseable {
    protected constructor(channel: FileChannel, position: Long, size: Long, shared: Boolean)

    fun channel(): FileChannel
    open fun acquiredBy(): Channel
    fun position(): Long
    fun size(): Long
    fun isShared(): Boolean
    val isShared: Boolean
    fun overlaps(position: Long, size: Long): Boolean
    abstract fun isValid(): Boolean
    val isValid: Boolean
    abstract fun release()
    override fun close()
}
