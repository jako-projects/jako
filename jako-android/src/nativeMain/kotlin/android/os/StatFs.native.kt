package android.os

import android.content.platformAppCacheDir
import android.content.platformAppDataDir
import android.content.platformDefaultExternalStorageDir
import java.io.File
import kotlinx.cinterop.sizeOf
import platform.posix.getpid

actual open class StatFs actual constructor(path: String?) {
    private var mBlockSize: Long = 4096L
    private var mTotalBytes: Long = 0L
    private var mFreeBytes: Long = 0L
    private var mAvailableBytes: Long = 0L

    init {
        restat(path)
    }

    actual fun restat(path: String?) {
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

    actual val blockSize: Int get() = getBlockSize()
    actual val blockSizeLong: Long get() = getBlockSizeLong()
    actual val blockCount: Int get() = getBlockCount()
    actual val blockCountLong: Long get() = getBlockCountLong()
    actual val freeBlocks: Int get() = getFreeBlocks()
    actual val freeBlocksLong: Long get() = getFreeBlocksLong()
    actual val freeBytes: Long get() = getFreeBytes()
    actual val availableBlocks: Int get() = getAvailableBlocks()
    actual val availableBlocksLong: Long get() = getAvailableBlocksLong()
    actual val availableBytes: Long get() = getAvailableBytes()
    actual val totalBytes: Long get() = getTotalBytes()

    actual fun getBlockSize(): Int = mBlockSize.toInt()
    actual fun getBlockSizeLong(): Long = mBlockSize
    actual fun getBlockCount(): Int = getBlockCountLong().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    actual fun getBlockCountLong(): Long = if (mBlockSize > 0) mTotalBytes / mBlockSize else 0L
    actual fun getFreeBlocks(): Int = getFreeBlocksLong().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    actual fun getFreeBlocksLong(): Long = if (mBlockSize > 0) mFreeBytes / mBlockSize else 0L
    actual fun getFreeBytes(): Long = mFreeBytes
    actual fun getAvailableBlocks(): Int = getAvailableBlocksLong().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    actual fun getAvailableBlocksLong(): Long = if (mBlockSize > 0) mAvailableBytes / mBlockSize else 0L
    actual fun getAvailableBytes(): Long = mAvailableBytes
    actual fun getTotalBytes(): Long = mTotalBytes
}
