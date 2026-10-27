package java.util.zip

import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream

actual open class InflaterOutputStream : FilterOutputStream {
    protected val inf: Inflater
    protected val buf: ByteArray
    private val wbuf = ByteArray(1)
    private var usesDefaultInflater = false
    private var closed = false

    actual constructor(out: OutputStream) : this(out, Inflater()) {
        usesDefaultInflater = true
    }

    actual constructor(out: OutputStream, infl: Inflater) : this(out, infl, 512)

    actual constructor(out: OutputStream, infl: Inflater, bufLen: Int) : super(out) {
        if (bufLen <= 0) throw IllegalArgumentException("buffer size <= 0")
        this.inf = infl
        this.buf = ByteArray(bufLen)
    }

    private fun ensureOpen() {
        if (closed) throw IOException("Stream closed")
    }

    actual override fun close() {
        if (!closed) {
            try {
                finish()
            } finally {
                out.close()
                closed = true
            }
        }
    }

    actual override fun flush() {
        ensureOpen()
        if (!inf.finished()) {
            try {
                while (!inf.finished() && !inf.needsInput()) {
                    val n = inf.inflate(buf, 0, buf.size)
                    if (n < 1) break
                    out.write(buf, 0, n)
                }
                super.flush()
            } catch (ex: DataFormatException) {
                throw ZipException(ex.message ?: "Invalid ZLIB data format")
            }
        }
    }

    actual open fun finish() {
        ensureOpen()
        flush()
        if (usesDefaultInflater) {
            inf.end()
        }
    }

    actual override fun write(b: Int) {
        wbuf[0] = b.toByte()
        write(wbuf, 0, 1)
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        ensureOpen()
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return
        try {
            var currentOff = off
            var remaining = len
            while (true) {
                if (inf.needsInput()) {
                    if (remaining < 1) break
                    val part = minOf(remaining, 512)
                    inf.setInput(b, currentOff, part)
                    currentOff += part
                    remaining -= part
                }
                var n: Int
                do {
                    n = inf.inflate(buf, 0, buf.size)
                    if (n > 0) {
                        out.write(buf, 0, n)
                    }
                } while (n > 0)
                if (inf.finished()) break
                if (inf.needsDictionary()) {
                    throw ZipException("ZLIB dictionary missing")
                }
            }
        } catch (ex: DataFormatException) {
            throw ZipException(ex.message ?: "Invalid ZLIB data format")
        }
    }
}
