package java.io

import java.util.Enumeration

actual open class SequenceInputStream : InputStream {
    private var e: Enumeration<out InputStream>? = null
    private var `in`: InputStream? = null

    actual constructor(e: Enumeration<out InputStream>) {
        this.e = e
        nextStream()
    }

    actual constructor(s1: InputStream, s2: InputStream) {
        val vector = listOf(s1, s2)
        var idx = 0
        this.e = object : Enumeration<InputStream> {
            override fun hasMoreElements(): Boolean = idx < vector.size
            override fun nextElement(): InputStream = vector[idx++]
        }
        nextStream()
    }

    private fun nextStream() {
        `in`?.close()
        `in` = null
        if (e != null && e!!.hasMoreElements()) {
            `in` = e!!.nextElement()
            if (`in` == null) throw NullPointerException()
        }
    }

    actual override fun available(): Int {
        return `in`?.available() ?: 0
    }

    actual override fun read(): Int {
        while (`in` != null) {
            val c = `in`!!.read()
            if (c != -1) return c
            nextStream()
        }
        return -1
    }

    actual override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (`in` == null) return -1
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0

        while (`in` != null) {
            val n = `in`!!.read(b, off, len)
            if (n > 0) return n
            nextStream()
        }
        return -1
    }

    actual override fun close() {
        while (`in` != null) {
            nextStream()
        }
    }
}
