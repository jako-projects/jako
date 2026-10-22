package java.util.zip

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.zlib.Z_BUF_ERROR
import platform.zlib.Z_DATA_ERROR
import platform.zlib.Z_MEM_ERROR
import platform.zlib.Z_NEED_DICT
import platform.zlib.Z_NO_FLUSH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.Z_STREAM_ERROR
import platform.zlib.deflate
import platform.zlib.deflateEnd
import platform.zlib.deflateInit2
import platform.zlib.deflateParams
import platform.zlib.deflateReset
import platform.zlib.deflateSetDictionary
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2
import platform.zlib.inflateReset
import platform.zlib.inflateSetDictionary
import platform.zlib.uBytefVar
import platform.zlib.z_stream_s

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual open class Inflater {
    private val zStream: z_stream_s
    private var input: ByteArray = EMPTY_BYTES
    private var inputOff = 0
    private var inputLen = 0
    private var finishedFlag = false
    private var needDict = false
    private var ended = false

    actual constructor() : this(false)

    actual constructor(nowrap: Boolean) {
        zStream = nativeHeap.alloc<z_stream_s> {
            zalloc = null
            zfree = null
            opaque = null
        }
        val windowBits = if (nowrap) -15 else 15
        val rc = inflateInit2(zStream.ptr, windowBits)
        if (rc != Z_OK) {
            nativeHeap.free(zStream)
            throw RuntimeException("inflateInit2 failed: $rc")
        }
    }

    actual fun setInput(b: ByteArray) {
        setInput(b, 0, b.size)
    }

    actual fun setInput(b: ByteArray, off: Int, len: Int) {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        input = b
        inputOff = off
        inputLen = len
    }

    actual fun setDictionary(b: ByteArray) {
        setDictionary(b, 0, b.size)
    }

    actual fun setDictionary(b: ByteArray, off: Int, len: Int) {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        val rc = if (len == 0) {
            inflateSetDictionary(zStream.ptr, null, 0u)
        } else {
            b.usePinned { pinned ->
                inflateSetDictionary(
                    zStream.ptr,
                    pinned.addressOf(off).reinterpret<uBytefVar>(),
                    len.toUInt()
                )
            }
        }
        if (rc != Z_OK) {
            throw IllegalArgumentException("inflateSetDictionary failed: $rc")
        }
        needDict = false
    }

    actual fun getRemaining(): Int = inputLen
    actual val remaining: Int get() = getRemaining()

    actual fun needsInput(): Boolean = inputLen <= 0

    actual fun needsDictionary(): Boolean = needDict

    actual fun finished(): Boolean = finishedFlag

    actual fun inflate(b: ByteArray): Int = inflate(b, 0, b.size)

    actual fun inflate(b: ByteArray, off: Int, len: Int): Int {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0 || finishedFlag) return 0
        val inLen = inputLen
        var produced = 0
        val result = if (inLen > 0) {
            input.usePinned { pinnedIn ->
                b.usePinned { pinnedOut ->
                    zStream.next_in = pinnedIn.addressOf(inputOff).reinterpret()
                    zStream.avail_in = inLen.toUInt()
                    zStream.next_out = pinnedOut.addressOf(off).reinterpret()
                    zStream.avail_out = len.toUInt()
                    val rc = inflate(zStream.ptr, Z_NO_FLUSH)
                    val consumed = inLen - zStream.avail_in.toInt()
                    inputOff += consumed
                    inputLen = zStream.avail_in.toInt()
                    produced = len - zStream.avail_out.toInt()
                    rc
                }
            }
        } else {
            b.usePinned { pinnedOut ->
                zStream.next_in = null
                zStream.avail_in = 0u
                zStream.next_out = pinnedOut.addressOf(off).reinterpret()
                zStream.avail_out = len.toUInt()
                val rc = inflate(zStream.ptr, Z_NO_FLUSH)
                produced = len - zStream.avail_out.toInt()
                rc
            }
        }
        zStream.next_in = null
        zStream.next_out = null
        when (result) {
            Z_OK, Z_BUF_ERROR -> {}
            Z_STREAM_END -> finishedFlag = true
            Z_NEED_DICT -> needDict = true
            Z_DATA_ERROR -> throw DataFormatException(zStream.msg?.toKString() ?: "invalid stored block lengths")
            Z_MEM_ERROR -> throw OutOfMemoryError("zlib inflate out of memory")
            Z_STREAM_ERROR -> throw DataFormatException("invalid compression stream")
            else -> throw DataFormatException("inflate failed: $result")
        }
        return produced
    }

    actual fun getAdler(): Int = ensureOpen().let { zStream.adler.toInt() }
    actual val adler: Int get() = getAdler()
    actual fun getTotalIn(): Int = getBytesRead().toInt()
    actual val totalIn: Int get() = getTotalIn()
    actual fun getTotalOut(): Int = getBytesWritten().toInt()
    actual val totalOut: Int get() = getTotalOut()
    actual fun getBytesRead(): Long {
        ensureOpen()
        return zStream.total_in.toLong()
    }
    actual val bytesRead: Long get() = getBytesRead()
    actual fun getBytesWritten(): Long {
        ensureOpen()
        return zStream.total_out.toLong()
    }
    actual val bytesWritten: Long get() = getBytesWritten()

    actual fun reset() {
        ensureOpen()
        inflateReset(zStream.ptr)
        input = EMPTY_BYTES
        inputOff = 0
        inputLen = 0
        finishedFlag = false
        needDict = false
    }

    actual fun end() {
        if (ended) return
        ended = true
        inflateEnd(zStream.ptr)
        nativeHeap.free(zStream)
    }

    private fun ensureOpen() {
        if (ended) throw NullPointerException("Inflater has been closed")
    }
}
