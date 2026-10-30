@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport

/**
 * Retrieve overall information about the space on a filesystem.
 */
@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
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
