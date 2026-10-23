package java.nio

internal fun readS64(bytes: ByteArray, off: Int, order: ByteOrder): Long {
    var v = 0L
    if (order == ByteOrder.BIG_ENDIAN) {
        for (i in 0 until 8) {
            v = (v shl 8) or (bytes[off + i].toLong() and 0xffL)
        }
    } else {
        for (i in 7 downTo 0) {
            v = (v shl 8) or (bytes[off + i].toLong() and 0xffL)
        }
    }
    return v
}

internal fun writeS64(bytes: ByteArray, off: Int, value: Long, order: ByteOrder) {
    if (order == ByteOrder.BIG_ENDIAN) {
        var shift = 56
        for (i in 0 until 8) {
            bytes[off + i] = (value ushr shift).toByte()
            shift -= 8
        }
    } else {
        var shift = 0
        for (i in 0 until 8) {
            bytes[off + i] = (value ushr shift).toByte()
            shift += 8
        }
    }
}

actual open class LongBuffer internal constructor(
    capacity: Int,
    private val hb: LongArray?,
    private val offset: Int,
    private val readOnly: Boolean,
    private val byteView: ByteArray?,
    private val byteOffset: Int,
    private val byteOrder: ByteOrder,
    private val isDirectBuffer: Boolean
) : Buffer(capacity, capacity), Comparable<LongBuffer> {

    actual open fun get(): Long {
        val p = position()
        if (p >= limit()) throw BufferUnderflowException()
        val v = getAbsolute(p)
        position(p + 1)
        return v
    }

    actual open fun put(l: Long): LongBuffer {
        if (readOnly) throw ReadOnlyBufferException()
        val p = position()
        if (p >= limit()) throw BufferOverflowException()
        setAbsolute(p, l)
        position(p + 1)
        return this
    }

    actual open fun get(index: Int): Long {
        if (index < 0 || index >= limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        return getAbsolute(index)
    }

    actual open fun put(index: Int, l: Long): LongBuffer {
        if (readOnly) throw ReadOnlyBufferException()
        if (index < 0 || index >= limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        setAbsolute(index, l)
        return this
    }

    actual open fun get(dst: LongArray, offset: Int, length: Int): LongBuffer {
        if (offset < 0 || length < 0 || offset + length > dst.size) throw IndexOutOfBoundsException()
        if (remaining() < length) throw BufferUnderflowException()
        val p = position()
        for (i in 0 until length) {
            dst[offset + i] = getAbsolute(p + i)
        }
        position(p + length)
        return this
    }

    actual open fun get(dst: LongArray): LongBuffer = get(dst, 0, dst.size)

    actual open fun put(src: LongArray, offset: Int, length: Int): LongBuffer {
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

    actual open fun put(src: LongArray): LongBuffer = put(src, 0, src.size)

    actual open fun put(src: LongBuffer): LongBuffer {
        if (src === this) throw IllegalArgumentException("Cannot put buffer into itself")
        if (readOnly) throw ReadOnlyBufferException()
        val n = src.remaining()
        if (n > remaining()) throw BufferOverflowException()
        for (i in 0 until n) {
            put(src.get())
        }
        return this
    }

    actual open fun order(): ByteOrder = byteOrder

    actual open fun asReadOnlyBuffer(): LongBuffer {
        val b = copyView(true)
        b.limit(limit())
        b.position(position())
        return b
    }

    actual open fun duplicate(): LongBuffer {
        val b = copyView(readOnly)
        b.limit(limit())
        b.position(position())
        return b
    }

    actual open fun slice(): LongBuffer {
        val rem = remaining()
        val p = position()
        return LongBuffer(
            rem, hb, offset + p, readOnly,
            byteView, byteOffset + p * 8, byteOrder, isDirectBuffer
        )
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

    actual override fun compareTo(other: LongBuffer): Int {
        val n = minOf(this.remaining(), other.remaining())
        val p1 = this.position()
        val p2 = other.position()
        for (i in 0 until n) {
            val cmp = this.get(p1 + i).compareTo(other.get(p2 + i))
            if (cmp != 0) return cmp
        }
        return this.remaining() - other.remaining()
    }

    private fun getAbsolute(index: Int): Long {
        return if (byteView != null) {
            readS64(byteView, byteOffset + index * 8, byteOrder)
        } else {
            hb!![offset + index]
        }
    }

    private fun setAbsolute(index: Int, v: Long) {
        if (byteView != null) {
            writeS64(byteView, byteOffset + index * 8, v, byteOrder)
        } else {
            hb!![offset + index] = v
        }
    }

    private fun copyView(readOnly: Boolean): LongBuffer {
        return LongBuffer(capacity(), hb, offset, readOnly, byteView, byteOffset, byteOrder, isDirectBuffer)
    }

    actual companion object {
        actual fun allocate(capacity: Int): LongBuffer {
            if (capacity < 0) throw IllegalArgumentException("capacity < 0: $capacity")
            return LongBuffer(capacity, LongArray(capacity), 0, false, null, 0, ByteOrder.nativeOrder(), false)
        }

        actual fun wrap(array: LongArray, offset: Int, length: Int): LongBuffer {
            if (offset < 0 || length < 0 || offset + length > array.size) throw IndexOutOfBoundsException()
            val b = LongBuffer(array.size, array, 0, false, null, 0, ByteOrder.nativeOrder(), false)
            b.position(offset)
            b.limit(offset + length)
            return b
        }

        actual fun wrap(array: LongArray): LongBuffer = wrap(array, 0, array.size)

        internal fun viewOfBytes(
            bytes: ByteArray,
            byteOffset: Int,
            capacity: Int,
            order: ByteOrder,
            readOnly: Boolean,
            isDirect: Boolean
        ): LongBuffer = LongBuffer(capacity, null, 0, readOnly, bytes, byteOffset, order, isDirect)
    }
}
