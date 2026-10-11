package java.io

actual open class CharArrayWriter : Writer {
    protected var buf: CharArray
    protected var count: Int = 0

    actual constructor() : this(32)

    actual constructor(initialSize: Int) {
        if (initialSize < 0) {
            throw IllegalArgumentException("Negative initial size: $initialSize")
        }
        buf = CharArray(initialSize)
    }

    private fun ensureCapacity(minCapacity: Int) {
        if (minCapacity - buf.size > 0) {
            var newCapacity = buf.size * 2
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity
            }
            val newBuf = CharArray(newCapacity)
            buf.copyInto(newBuf, 0, 0, count)
            buf = newBuf
        }
    }

    actual override fun write(c: Int) {
        ensureCapacity(count + 1)
        buf[count++] = c.toChar()
    }

    actual override fun write(cbuf: CharArray, off: Int, len: Int) {
        if (off < 0 || len < 0 || len > cbuf.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, cbuf.size=${cbuf.size}")
        }
        if (len == 0) return
        ensureCapacity(count + len)
        cbuf.copyInto(buf, count, off, off + len)
        count += len
    }

    actual override fun write(str: String, off: Int, len: Int) {
        if (off < 0 || len < 0 || len > str.length - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, str.length=${str.length}")
        }
        if (len == 0) return
        ensureCapacity(count + len)
        for (i in 0 until len) {
            buf[count + i] = str[off + i]
        }
        count += len
    }

    actual open fun writeTo(out: Writer) {
        out.write(buf, 0, count)
    }

    actual open fun toCharArray(): CharArray {
        val result = CharArray(count)
        buf.copyInto(result, 0, 0, count)
        return result
    }

    actual open fun reset() {
        count = 0
    }

    actual open fun size(): Int = count

    actual override fun toString(): String {
        return buf.concatToString(0, count)
    }

    actual override fun flush() {}

    actual override fun close() {}
}
