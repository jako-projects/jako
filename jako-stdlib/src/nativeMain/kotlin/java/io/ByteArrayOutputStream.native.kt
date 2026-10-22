package java.io

actual open class ByteArrayOutputStream : OutputStream {
    protected var buf: ByteArray
    protected var count: Int = 0

    actual constructor() : this(32)

    actual constructor(size: Int) : super() {
        if (size < 0) {
            throw IllegalArgumentException("Negative initial size: $size")
        }
        this.buf = ByteArray(size)
    }

    private fun ensureCapacity(minCapacity: Int) {
        if (minCapacity - buf.size > 0) {
            var newCapacity = buf.size shl 1
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity
            }
            buf = buf.copyOf(newCapacity)
        }
    }

    actual override fun write(b: Int) {
        ensureCapacity(count + 1)
        buf[count] = b.toByte()
        count += 1
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return
        ensureCapacity(count + len)
        b.copyInto(buf, destinationOffset = count, startIndex = off, endIndex = off + len)
        count += len
    }

    actual open fun reset() {
        count = 0
    }

    actual open fun toByteArray(): ByteArray {
        return buf.copyOf(count)
    }

    actual open fun size(): Int = count

    fun toString(charsetName: String): String {
        return buf.decodeToString(0, count)
    }

    override fun toString(): String {
        return buf.decodeToString(0, count)
    }

    override fun close() {}
}
