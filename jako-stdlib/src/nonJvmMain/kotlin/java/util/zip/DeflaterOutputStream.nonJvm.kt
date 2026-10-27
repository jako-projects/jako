package java.util.zip

import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream

actual open class DeflaterOutputStream : FilterOutputStream {
    protected var def: Deflater
    protected var buf: ByteArray
    protected var usesDefaultDeflater: Boolean = false
    private var closed: Boolean = false
    private val syncFlush: Boolean
    private val singleByte = ByteArray(1)

    actual constructor(outStream: OutputStream) : this(outStream, Deflater(), 512, false) {
        usesDefaultDeflater = true
    }

    actual constructor(outStream: OutputStream, def: Deflater) : this(outStream, def, 512, false)

    actual constructor(outStream: OutputStream, def: Deflater, size: Int) : this(outStream, def, size, false)

    actual constructor(
        outStream: OutputStream,
        def: Deflater,
        size: Int,
        syncFlush: Boolean
    ) : super(outStream) {
        if (size <= 0) throw IllegalArgumentException("buffer size <= 0")
        this.def = def
        this.buf = ByteArray(size)
        this.syncFlush = syncFlush
    }

    actual constructor(outStream: OutputStream, def: Deflater, syncFlush: Boolean) :
        this(outStream, def, 512, syncFlush)

    actual constructor(outStream: OutputStream, syncFlush: Boolean) : this(outStream, Deflater(), 512, syncFlush) {
        usesDefaultDeflater = true
    }

    protected fun ensureOpen() {
        if (closed) throw IOException("Stream closed")
    }

    actual override fun write(b: Int) {
        singleByte[0] = b.toByte()
        write(singleByte, 0, 1)
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return
        if (def.finished()) {
            throw IOException("write beyond end of stream")
        }
        def.setInput(b, off, len)
        while (!def.needsInput()) {
            deflate()
        }
    }

    protected open fun deflate() {
        val n = def.deflate(buf, 0, buf.size)
        if (n > 0) {
            out.write(buf, 0, n)
        }
    }

    actual open fun finish() {
        ensureOpen()
        if (!def.finished()) {
            def.finish()
            while (!def.finished()) {
                deflate()
            }
        }
    }

    actual override fun close() {
        if (closed) return
        try {
            finish()
        } finally {
            if (usesDefaultDeflater) {
                def.end()
            }
            out.close()
            closed = true
        }
    }

    actual override fun flush() {
        ensureOpen()
        if (syncFlush && !def.finished()) {
            var n: Int
            do {
                n = def.deflate(buf, 0, buf.size, Deflater.SYNC_FLUSH)
                if (n > 0) out.write(buf, 0, n)
            } while (n > 0)
        }
        out.flush()
    }
}
