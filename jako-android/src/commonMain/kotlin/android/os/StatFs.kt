@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

/**
 * Retrieve overall information about the space on a filesystem.
 */
expect open class StatFs(path: String?) {
    val blockSize: Int
    val blockSizeLong: Long
    val blockCount: Int
    val blockCountLong: Long
    val freeBlocks: Int
    val freeBlocksLong: Long
    val freeBytes: Long
    val availableBlocks: Int
    val availableBlocksLong: Long
    val availableBytes: Long
    val totalBytes: Long

    fun restat(path: String?)
    fun getBlockSize(): Int
    fun getBlockSizeLong(): Long
    fun getBlockCount(): Int
    fun getBlockCountLong(): Long
    fun getFreeBlocks(): Int
    fun getFreeBlocksLong(): Long
    fun getFreeBytes(): Long
    fun getAvailableBlocks(): Int
    fun getAvailableBlocksLong(): Long
    fun getAvailableBytes(): Long
    fun getTotalBytes(): Long
}
