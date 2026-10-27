package java.util.zip

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

actual open class DeflaterInputStream : FilterInputStream {
    protected val def: Deflater
    protected val buf: ByteArray
    private val rbuf = ByteArray(1)
    private var usesDefaultDeflater = false
    private var reachEOF = false
    private var closed = false

    actual constructor(inStream: InputStream) : this(inStream, Deflater()) {
        usesDefaultDeflater = true
    }

    actual constructor(inStream: InputStream, defl: Deflater) : this(inStream, defl, 512)

    actual constructor(inStream: InputStream, defl: Deflater, bufLen: Int) : super(inStream) {
        if (bufLen <= 0) throw IllegalArgumentException("buffer size <= 0")
        this.def = defl
        this.buf = ByteArray(bufLen)
    }

    private fun ensureOpen() {
        if (closed) throw IOException("Stream closed")
    }

    actual override fun close() {
        if (!closed) {
            try {
                if (usesDefaultDeflater) {
                    def.end()
                }
                inStream.close()
            } finally {
                closed = true
            }
        }
    }

    actual override fun read(): Int {
        val len = read(rbuf, 0, 1)
        return if (len <= 0) -1 else (rbuf[0].toInt() and 0xFF)
    }

    actual override fun read(b: ByteArray, off: Int, len: Int): Int {
        ensureOpen()
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0
        var count = 0
        while (count < len && !def.finished()) {
            if (def.needsInput()) {
                val n = inStream.read(buf, 0, buf.size)
                if (n < 0) {
                    def.finish()
                } else if (n > 0) {
                    def.setInput(buf, 0, n)
                }
            }
            val n = def.deflate(b, off + count, len - count)
            count += n
        }
        if (count == 0 && def.finished()) {
            reachEOF = true
            count = -1
        }
        return count
    }

    actual override fun skip(n: Long): Long {
        if (n < 0) throw IllegalArgumentException("negative skip length")
        ensureOpen()
        if (rbuf.size < 512) {
            val skipBuf = ByteArray(minOf(n, 512L).toInt())
            var total = 0L
            while (total < n) {
                val r = read(skipBuf, 0, minOf(n - total, skipBuf.size.toLong()).toInt())
                if (r < 0) break
                total += r
            }
            return total
        }
        return 0L
    }

    actual override fun available(): Int {
        ensureOpen()
        return if (reachEOF) 0 else 1
    }

    actual override fun markSupported(): Boolean = false

    actual override fun mark(limit: Int) {}

    actual override fun reset() {
        throw IOException("mark/reset not supported")
    }
}
