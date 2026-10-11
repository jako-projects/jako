@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

import java.io.Closeable
import java.io.File
import java.io.FilterInputStream
import java.io.FilterOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Cloneable
import java.util.Enumeration

expect interface Checksum {
    fun update(b: Int)
    fun update(b: ByteArray, off: Int, len: Int)
    fun getValue(): Long
    val value: Long
    fun reset()
}

expect class CRC32() : Checksum {
    override fun update(b: Int)
    override fun update(b: ByteArray, off: Int, len: Int)
    fun update(b: ByteArray)
    override fun getValue(): Long
    override val value: Long
    override fun reset()
}

expect class Adler32() : Checksum {
    override fun update(b: Int)
    override fun update(b: ByteArray, off: Int, len: Int)
    fun update(b: ByteArray)
    override fun getValue(): Long
    override val value: Long
    override fun reset()
}

expect open class ZipException : IOException {
    constructor()
    constructor(s: String?)
}

expect open class DataFormatException : Exception {
    constructor()
    constructor(s: String?)
}

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

expect open class Deflater {
    constructor()
    constructor(level: Int)
    constructor(level: Int, nowrap: Boolean)

    fun setInput(b: ByteArray)
    fun setInput(b: ByteArray, off: Int, len: Int)
    fun setDictionary(b: ByteArray)
    fun setDictionary(b: ByteArray, off: Int, len: Int)
    fun setStrategy(strategy: Int)
    fun setLevel(level: Int)
    fun needsInput(): Boolean
    fun finish()
    fun finished(): Boolean
    fun deflate(b: ByteArray): Int
    fun deflate(b: ByteArray, off: Int, len: Int): Int
    fun deflate(b: ByteArray, off: Int, len: Int, flush: Int): Int
    fun getAdler(): Int
    val adler: Int
    fun getTotalIn(): Int
    val totalIn: Int
    fun getTotalOut(): Int
    val totalOut: Int
    fun getBytesRead(): Long
    val bytesRead: Long
    fun getBytesWritten(): Long
    val bytesWritten: Long
    fun reset()
    fun end()

    companion object {
        val DEFLATED: Int
        val NO_COMPRESSION: Int
        val BEST_SPEED: Int
        val BEST_COMPRESSION: Int
        val DEFAULT_COMPRESSION: Int
        val FILTERED: Int
        val HUFFMAN_ONLY: Int
        val DEFAULT_STRATEGY: Int
        val NO_FLUSH: Int
        val SYNC_FLUSH: Int
        val FULL_FLUSH: Int
    }
}

expect open class Inflater {
    constructor()
    constructor(nowrap: Boolean)

    fun setInput(b: ByteArray)
    fun setInput(b: ByteArray, off: Int, len: Int)
    fun setDictionary(b: ByteArray)
    fun setDictionary(b: ByteArray, off: Int, len: Int)
    fun getRemaining(): Int
    val remaining: Int
    fun needsInput(): Boolean
    fun needsDictionary(): Boolean
    fun finished(): Boolean
    fun inflate(b: ByteArray): Int
    fun inflate(b: ByteArray, off: Int, len: Int): Int
    fun getAdler(): Int
    val adler: Int
    fun getTotalIn(): Int
    val totalIn: Int
    fun getTotalOut(): Int
    val totalOut: Int
    fun getBytesRead(): Long
    val bytesRead: Long
    fun getBytesWritten(): Long
    val bytesWritten: Long
    fun reset()
    fun end()
}

expect open class InflaterInputStream : FilterInputStream {
    constructor(inStream: InputStream)
    constructor(inStream: InputStream, inf: Inflater)
    constructor(inStream: InputStream, inf: Inflater, size: Int)

    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun available(): Int
    override fun skip(n: Long): Long
    override fun close()
    override fun markSupported(): Boolean
    override fun mark(readlimit: Int)
    override fun reset()
}

expect open class DeflaterOutputStream : FilterOutputStream {
    constructor(outStream: OutputStream)
    constructor(outStream: OutputStream, def: Deflater)
    constructor(outStream: OutputStream, def: Deflater, size: Int)
    constructor(outStream: OutputStream, def: Deflater, size: Int, syncFlush: Boolean)
    constructor(outStream: OutputStream, def: Deflater, syncFlush: Boolean)
    constructor(outStream: OutputStream, syncFlush: Boolean)

    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    open fun finish()
    override fun close()
    override fun flush()
}

expect open class GZIPInputStream : InflaterInputStream {
    constructor(inStream: InputStream)
    constructor(inStream: InputStream, size: Int)

    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun close()

    companion object {
        val GZIP_MAGIC: Int
    }
}

expect open class GZIPOutputStream : DeflaterOutputStream {
    constructor(outStream: OutputStream)
    constructor(outStream: OutputStream, size: Int)
    constructor(outStream: OutputStream, size: Int, syncFlush: Boolean)
    constructor(outStream: OutputStream, syncFlush: Boolean)

    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun finish()
}

expect open class ZipInputStream : InflaterInputStream {
    constructor(inStream: InputStream)

    // [Nullability Decision] getNextEntry returns null at end of archive (Java 8).
    open fun getNextEntry(): ZipEntry?
    open fun closeEntry()
    override fun available(): Int
    override fun read(): Int
    override fun read(b: ByteArray, off: Int, len: Int): Int
    override fun close()
    protected open fun createZipEntry(name: String): ZipEntry
}

expect open class ZipOutputStream : DeflaterOutputStream {
    constructor(outStream: OutputStream)

    // [Nullability Decision] setComment accepts null to clear the archive comment (Java 8).
    open fun setComment(comment: String?)
    open fun setMethod(method: Int)
    open fun setLevel(level: Int)
    open fun putNextEntry(e: ZipEntry)
    open fun closeEntry()
    override fun write(b: Int)
    override fun write(b: ByteArray, off: Int, len: Int)
    override fun finish()
    override fun close()

    companion object {
        val STORED: Int
        val DEFLATED: Int
    }
}

expect open class ZipFile : Closeable {
    constructor(name: String)
    constructor(file: File)
    constructor(file: File, mode: Int)

    // [Nullability Decision] getComment is null when the archive has no comment (Java 8).
    open fun getComment(): String?
    open val comment: String?
    // [Nullability Decision] getEntry is null when the name is absent (Java 8).
    open fun getEntry(name: String): ZipEntry?
    // [Nullability Decision] getInputStream is null when the entry name is absent (Java 8).
    open fun getInputStream(entry: ZipEntry): InputStream?
    open fun getName(): String
    open val name: String
    open fun entries(): Enumeration<out ZipEntry>
    open fun size(): Int
    override fun close()

    companion object {
        val OPEN_READ: Int
        val OPEN_DELETE: Int
    }
}
