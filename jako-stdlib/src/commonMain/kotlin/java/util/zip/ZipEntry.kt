@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

import java.lang.Cloneable

expect open class ZipEntry : Cloneable {
    constructor(name: String)
    constructor(e: ZipEntry)

    open fun getName(): String
    open fun setTime(time: Long)
    open fun getTime(): Long
    open fun setSize(size: Long)
    open fun getSize(): Long
    open fun setCompressedSize(csize: Long)
    open fun getCompressedSize(): Long
    open fun setCrc(crc: Long)
    open fun getCrc(): Long
    open fun setMethod(method: Int)
    open fun getMethod(): Int
    // [Nullability Decision] comment / extra are optional ZIP metadata; Java 8 returns null when unset.
    open fun setComment(comment: String?)
    open fun getComment(): String?
    open fun setExtra(extra: ByteArray?)
    open fun getExtra(): ByteArray?
    open fun isDirectory(): Boolean

    open val name: String
    open var time: Long
    open var size: Long
    open var compressedSize: Long
    open var crc: Long
    open var method: Int
    open var comment: String?
    open var extra: ByteArray?
    open val isDirectory: Boolean

    companion object {
        val STORED: Int
        val DEFLATED: Int
    }
}
