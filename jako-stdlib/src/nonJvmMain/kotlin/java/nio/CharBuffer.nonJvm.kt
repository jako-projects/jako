package java.nio

internal fun readU16(bytes: ByteArray, off: Int, order: ByteOrder): Int {
    val b0 = bytes[off].toInt() and 0xff
    val b1 = bytes[off + 1].toInt() and 0xff
    return if (order == ByteOrder.BIG_ENDIAN) (b0 shl 8) or b1 else (b1 shl 8) or b0
}

internal fun writeU16(bytes: ByteArray, off: Int, value: Int, order: ByteOrder) {
    val v = value and 0xffff
    if (order == ByteOrder.BIG_ENDIAN) {
        bytes[off] = (v shr 8).toByte()
        bytes[off + 1] = v.toByte()
    } else {
        bytes[off] = v.toByte()
        bytes[off + 1] = (v shr 8).toByte()
    }
}

actual open class CharBuffer internal constructor(
    capacity: Int,
    private val hb: CharArray?,
    private val offset: Int,
    private val readOnly: Boolean,
    private val byteView: ByteArray?,
    private val byteOffset: Int,
    private val byteOrder: ByteOrder,
    private val isDirectBuffer: Boolean
) : Buffer(capacity, capacity), Comparable<CharBuffer>, Appendable, java.lang.Readable {

    actual override fun read(target: CharBuffer): Int {
        val targetRemaining = target.remaining()
        val n = remaining()
        if (n == 0) return -1
        if (targetRemaining <= 0) return 0
        val toRead = minOf(n, targetRemaining)
        val savedLimit = limit()
        if (targetRemaining < n) {
            limit(position() + toRead)
        }
        try {
            if (toRead > 0) {
                target.put(this)
            }
        } finally {
            limit(savedLimit)
        }
        return toRead
    }

    actual open fun get(): Char {
        val p = position()
        if (p >= limit()) throw BufferUnderflowException()
        val c = getAbsolute(p)
        position(p + 1)
        return c
    }

    actual open fun put(c: Char): CharBuffer {
        if (readOnly) throw ReadOnlyBufferException()
        val p = position()
        if (p >= limit()) throw BufferOverflowException()
        setAbsolute(p, c)
        position(p + 1)
        return this
    }

    actual open fun get(index: Int): Char {
        if (index < 0 || index >= limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        return getAbsolute(index)
    }

    actual open fun put(index: Int, c: Char): CharBuffer {
        if (readOnly) throw ReadOnlyBufferException()
        if (index < 0 || index >= limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        setAbsolute(index, c)
        return this
    }

    actual open fun get(dst: CharArray, offset: Int, length: Int): CharBuffer {
        if (offset < 0 || length < 0 || offset + length > dst.size) throw IndexOutOfBoundsException()
        if (remaining() < length) throw BufferUnderflowException()
        val p = position()
        for (i in 0 until length) {
            dst[offset + i] = getAbsolute(p + i)
        }
        position(p + length)
        return this
    }

    actual open fun get(dst: CharArray): CharBuffer = get(dst, 0, dst.size)

    actual open fun put(src: CharArray, offset: Int, length: Int): CharBuffer {
        if (readOnly) throw ReadOnlyBufferException()
        if (offset < 0 || length < 0 || offset + length > src.size) throw IndexOutOfBoundsException()
        if (remaining() < length) throw BufferOverflowException()
        val p = position()
        for (i in 0 until length) {
            setAbsolute(p + i, src[offset + i])
        }
        position(p + length)
        return this
    }

    actual open fun put(src: CharArray): CharBuffer = put(src, 0, src.size)

    actual open fun put(src: CharBuffer): CharBuffer {
        if (src === this) throw IllegalArgumentException("Cannot put buffer into itself")
        if (readOnly) throw ReadOnlyBufferException()
        val n = src.remaining()
        if (n > remaining()) throw BufferOverflowException()
        for (i in 0 until n) {
            put(src.get())
        }
        return this
    }

    actual open fun put(src: String): CharBuffer = put(src, 0, src.length)

    actual open fun put(src: String, start: Int, end: Int): CharBuffer {
        if (start < 0 || end > src.length || start > end) throw IndexOutOfBoundsException()
        if (readOnly) throw ReadOnlyBufferException()
        val n = end - start
        if (n > remaining()) throw BufferOverflowException()
        val p = position()
        for (i in 0 until n) {
            setAbsolute(p + i, src[start + i])
        }
        position(p + n)
        return this
    }

    actual open fun order(): ByteOrder = byteOrder

    actual open fun asReadOnlyBuffer(): CharBuffer {
        val b = copyView(readOnly = true)
        b.limit(limit())
        b.position(position())
        return b
    }

    actual open fun duplicate(): CharBuffer {
        val b = copyView(readOnly)
        b.limit(limit())
        b.position(position())
        return b
    }

    actual open fun slice(): CharBuffer {
        val rem = remaining()
        val p = position()
        return CharBuffer(
            rem, hb, offset + p, readOnly,
            byteView, byteOffset + p * 2, byteOrder, isDirectBuffer
        )
    }

    actual open fun charAt(index: Int): Char {
        if (index < 0 || index >= remaining()) throw IndexOutOfBoundsException()
        return getAbsolute(position() + index)
    }

    actual open fun length(): Int = remaining()

    actual open fun subSequence(start: Int, end: Int): CharBuffer {
        if (start < 0 || end > remaining() || start > end) throw IndexOutOfBoundsException()
        val p = position()
        val b = CharBuffer(
            remaining(), hb, offset + p, readOnly,
            byteView, byteOffset + p * 2, byteOrder, isDirectBuffer
        )
        b.position(start)
        b.limit(end)
        return b
    }

    actual override fun isReadOnly(): Boolean = readOnly
    actual override fun hasArray(): Boolean = hb != null && !readOnly
    actual override fun array(): Any {
        if (hb == null) throw UnsupportedOperationException()
        if (readOnly) throw ReadOnlyBufferException()
        return hb
    }
    actual override fun arrayOffset(): Int {
        if (hb == null) throw UnsupportedOperationException()
        if (readOnly) throw ReadOnlyBufferException()
        return offset
    }
    actual override fun isDirect(): Boolean = isDirectBuffer

    actual override val isReadOnly: Boolean get() = isReadOnly()
    actual override val isDirect: Boolean get() = isDirect()
    actual val order: ByteOrder get() = order()

    actual override fun compareTo(other: CharBuffer): Int {
        val n = minOf(this.remaining(), other.remaining())
        val p1 = this.position()
        val p2 = other.position()
        for (i in 0 until n) {
            val cmp = this.get(p1 + i).compareTo(other.get(p2 + i))
            if (cmp != 0) return cmp
        }
        return this.remaining() - other.remaining()
    }

    actual override fun append(value: CharSequence?): CharBuffer = put(value?.toString() ?: "null")

    actual override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): CharBuffer {
        val cs = value ?: "null"
        return put(cs.subSequence(startIndex, endIndex).toString())
    }

    actual override fun append(value: Char): CharBuffer = put(value)

    override fun toString(): String {
        val p = position()
        val n = remaining()
        return CharArray(n) { getAbsolute(p + it) }.concatToString()
    }

    private fun getAbsolute(index: Int): Char {
        return if (byteView != null) {
            readU16(byteView, byteOffset + index * 2, byteOrder).toChar()
        } else {
            hb!![offset + index]
        }
    }

    private fun setAbsolute(index: Int, c: Char) {
        if (byteView != null) {
            writeU16(byteView, byteOffset + index * 2, c.code, byteOrder)
        } else {
            hb!![offset + index] = c
        }
    }

    private fun copyView(readOnly: Boolean): CharBuffer {
        return CharBuffer(capacity(), hb, offset, readOnly, byteView, byteOffset, byteOrder, isDirectBuffer)
    }

    actual companion object {
        actual fun allocate(capacity: Int): CharBuffer {
            if (capacity < 0) throw IllegalArgumentException("capacity < 0: $capacity")
            return CharBuffer(capacity, CharArray(capacity), 0, false, null, 0, ByteOrder.nativeOrder(), false)
        }

        actual fun wrap(array: CharArray, offset: Int, length: Int): CharBuffer {
            if (offset < 0 || length < 0 || offset + length > array.size) throw IndexOutOfBoundsException()
            val b = CharBuffer(array.size, array, 0, false, null, 0, ByteOrder.nativeOrder(), false)
            b.position(offset)
            b.limit(offset + length)
            return b
        }

        actual fun wrap(array: CharArray): CharBuffer = wrap(array, 0, array.size)

        actual fun wrap(csq: CharSequence): CharBuffer = wrap(csq, 0, csq.length)

        actual fun wrap(csq: CharSequence, start: Int, end: Int): CharBuffer {
            if (start < 0 || end > csq.length || start > end) throw IndexOutOfBoundsException()
            // Copy of the sequence: Java wraps a read-only view; equivalent for immutable String.
            val arr = CharArray(csq.length) { csq[it] }
            val b = CharBuffer(csq.length, arr, 0, true, null, 0, ByteOrder.nativeOrder(), false)
            b.position(start)
            b.limit(end)
            return b
        }

        internal fun viewOfBytes(
            bytes: ByteArray,
            byteOffset: Int,
            capacity: Int,
            order: ByteOrder,
            readOnly: Boolean,
            isDirect: Boolean
        ): CharBuffer = CharBuffer(capacity, null, 0, readOnly, bytes, byteOffset, order, isDirect)
    }
}
