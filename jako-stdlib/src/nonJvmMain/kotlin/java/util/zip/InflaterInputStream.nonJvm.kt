package java.util.zip

import java.io.EOFException
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

actual open class InflaterInputStream : FilterInputStream {
    protected var inf: Inflater
    protected var buf: ByteArray
    protected var len: Int = 0
    internal var usesDefaultInflater: Boolean = false
    protected var reachEOF: Boolean = false
    private var closed: Boolean = false
    private val singleByte = ByteArray(1)

    actual constructor(inStream: InputStream) : this(inStream, Inflater(), 512) {
        usesDefaultInflater = true
    }

    actual constructor(inStream: InputStream, inf: Inflater) : this(inStream, inf, 512)

    actual constructor(inStream: InputStream, inf: Inflater, size: Int) : super(inStream) {
        if (size <= 0) throw IllegalArgumentException("buffer size <= 0")
        this.inf = inf
        this.buf = ByteArray(size)
    }

    protected fun ensureOpen() {
        if (closed) throw IOException("Stream closed")
    }

    actual override fun read(): Int {
        val n = read(singleByte, 0, 1)
        return if (n <= 0) -1 else singleByte[0].toInt() and 0xFF
    }

    actual override fun read(b: ByteArray, off: Int, len: Int): Int {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0
        try {
            while (true) {
                val n = inf.inflate(b, off, len)
                if (n > 0) return n
                if (inf.finished() || inf.needsDictionary()) {
                    reachEOF = true
                    return -1
                }
                if (inf.needsInput()) {
                    fill()
                }
            }
        } catch (e: DataFormatException) {
            throw ZipException(e.message)
        }
    }

    protected open fun fill() {
        ensureOpen()
        len = inStream.read(buf, 0, buf.size)
        if (len == -1) {
            throw EOFException("Unexpected end of ZLIB input stream")
        }
        inf.setInput(buf, 0, len)
    }

    actual override fun available(): Int {
        ensureOpen()
        return if (reachEOF) 0 else 1
    }

    actual override fun skip(n: Long): Long {
        if (n < 0) throw IllegalArgumentException("negative skip")
        val max = minOf(n, Int.MAX_VALUE.toLong()).toInt()
        val tmp = ByteArray(minOf(512, max))
        var total = 0
        while (total < max) {
            val count = read(tmp, 0, minOf(max - total, tmp.size))
            if (count < 0) break
            total += count
        }
        return total.toLong()
    }

    actual override fun close() {
        if (closed) return
        if (usesDefaultInflater) {
            inf.end()
        }
        inStream.close()
        closed = true
    }

    actual override fun markSupported(): Boolean = false

    actual override fun mark(readlimit: Int) {}

    actual override fun reset() {
        throw IOException("mark/reset not supported")
    }
}
