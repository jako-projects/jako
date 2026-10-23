package java.util.zip

actual open class ZipEntry : Cloneable {
    private var entryName: String
    private var entryTime: Long = -1L
    private var entrySize: Long = -1L
    private var entryCsize: Long = -1L
    private var entryCrc: Long = -1L
    private var entryMethod: Int = -1
    private var entryComment: String? = null
    private var entryExtra: ByteArray? = null
    internal var flag: Int = 0
    internal var offset: Long = -1L

    actual constructor(name: String) {
        if (name.length > 0xFFFF) {
            throw IllegalArgumentException("entry name too long")
        }
        this.entryName = name
    }

    actual constructor(e: ZipEntry) {
        this.entryName = e.entryName
        this.entryTime = e.entryTime
        this.entrySize = e.entrySize
        this.entryCsize = e.entryCsize
        this.entryCrc = e.entryCrc
        this.entryMethod = e.entryMethod
        this.entryComment = e.entryComment
        this.entryExtra = e.entryExtra?.copyOf()
        this.flag = e.flag
        this.offset = e.offset
    }

    actual open fun getName(): String = entryName
    actual open fun setTime(time: Long) { this.entryTime = time }
    actual open fun getTime(): Long = entryTime
    actual open fun setSize(size: Long) { this.entrySize = size }
    actual open fun getSize(): Long = entrySize
    actual open fun setCompressedSize(csize: Long) { this.entryCsize = csize }
    actual open fun getCompressedSize(): Long = entryCsize
    actual open fun setCrc(crc: Long) { this.entryCrc = crc }
    actual open fun getCrc(): Long = entryCrc
    actual open fun setMethod(method: Int) { this.entryMethod = method }
    actual open fun getMethod(): Int = entryMethod
    actual open fun setComment(comment: String?) { this.entryComment = comment }
    actual open fun getComment(): String? = entryComment
    actual open fun setExtra(extra: ByteArray?) {
        if (extra != null && extra.size > 0xFFFF) {
            throw IllegalArgumentException("invalid extra field length")
        }
        this.entryExtra = extra
    }
    actual open fun getExtra(): ByteArray? = entryExtra
    actual open fun isDirectory(): Boolean = entryName.endsWith("/")

    actual open val name: String get() = getName()
    actual open var time: Long
        get() = getTime()
        set(value) = setTime(value)
    actual open var size: Long
        get() = getSize()
        set(value) = setSize(value)
    actual open var compressedSize: Long
        get() = getCompressedSize()
        set(value) = setCompressedSize(value)
    actual open var crc: Long
        get() = getCrc()
        set(value) = setCrc(value)
    actual open var method: Int
        get() = getMethod()
        set(value) = setMethod(value)
    actual open var comment: String?
        get() = getComment()
        set(value) = setComment(value)
    actual open var extra: ByteArray?
        get() = getExtra()
        set(value) = setExtra(value)
    actual open val isDirectory: Boolean get() = isDirectory()

    override fun toString(): String = entryName

    actual companion object {
        actual val STORED: Int = 0
        actual val DEFLATED: Int = 8
    }
}
