package android.os

import java.io.File

open class StatFs(path: String?) {
    private var mBlockSize: Long = 4096L
    private var mTotalBytes: Long = 0L
    private var mFreeBytes: Long = 0L
    private var mAvailableBytes: Long = 0L

    init {
        restat(path)
    }

    fun restat(path: String?) {
        val p = requireNotNull(path) { "StatFs path cannot be null" }
        val file = File(p)
        if (!file.exists()) {
            throw IllegalArgumentException("Failed to stat path: $p (does not exist)")
        }
        mBlockSize = 4096L
        mTotalBytes = maxOf(0L, file.totalSpace)
        mFreeBytes = maxOf(0L, file.freeSpace)
        mAvailableBytes = maxOf(0L, file.usableSpace)
    }

    fun getBlockSize(): Int = mBlockSize.toInt()
    fun getBlockSizeLong(): Long = mBlockSize
    fun getBlockCount(): Int = getBlockCountLong().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    fun getBlockCountLong(): Long = if (mBlockSize > 0) mTotalBytes / mBlockSize else 0L
    fun getFreeBlocks(): Int = getFreeBlocksLong().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    fun getFreeBlocksLong(): Long = if (mBlockSize > 0) mFreeBytes / mBlockSize else 0L
    fun getFreeBytes(): Long = mFreeBytes
    fun getAvailableBlocks(): Int = getAvailableBlocksLong().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    fun getAvailableBlocksLong(): Long = if (mBlockSize > 0) mAvailableBytes / mBlockSize else 0L
    fun getAvailableBytes(): Long = mAvailableBytes
    fun getTotalBytes(): Long = mTotalBytes
}
