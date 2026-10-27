package java.nio

actual open class FloatBuffer internal constructor(
    capacity: Int,
    private val hb: FloatArray?,
    private val offset: Int,
    private val readOnly: Boolean,
    private val byteView: ByteArray?,
    private val byteOffset: Int,
    private val byteOrder: ByteOrder,
    private val isDirectBuffer: Boolean
) : Buffer(capacity, capacity), Comparable<FloatBuffer> {

    actual open fun get(): Float {
        val p = position()
        if (p >= limit()) throw BufferUnderflowException()
        val v = getAbsolute(p)
        position(p + 1)
        return v
    }

    actual open fun put(f: Float): FloatBuffer {
        if (readOnly) throw ReadOnlyBufferException()
        val p = position()
        if (p >= limit()) throw BufferOverflowException()
        setAbsolute(p, f)
        position(p + 1)
        return this
    }

    actual open fun get(index: Int): Float {
        if (index < 0 || index >= limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        return getAbsolute(index)
    }

    actual open fun put(index: Int, f: Float): FloatBuffer {
        if (readOnly) throw ReadOnlyBufferException()
        if (index < 0 || index >= limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        setAbsolute(index, f)
        return this
    }

    actual open fun get(dst: FloatArray, offset: Int, length: Int): FloatBuffer {
        if (offset < 0 || length < 0 || offset + length > dst.size) throw IndexOutOfBoundsException()
        if (remaining() < length) throw BufferUnderflowException()
        val p = position()
        for (i in 0 until length) {
            dst[offset + i] = getAbsolute(p + i)
        }
        position(p + length)
        return this
    }

    actual open fun get(dst: FloatArray): FloatBuffer = get(dst, 0, dst.size)

    actual open fun put(src: FloatArray, offset: Int, length: Int): FloatBuffer {
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

    actual open fun put(src: FloatArray): FloatBuffer = put(src, 0, src.size)

    actual open fun put(src: FloatBuffer): FloatBuffer {
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

    actual open fun asReadOnlyBuffer(): FloatBuffer {
        val b = copyView(true)
        b.limit(limit())
        b.position(position())
        return b
    }

    actual open fun duplicate(): FloatBuffer {
        val b = copyView(readOnly)
        b.limit(limit())
        b.position(position())
        return b
    }

    actual open fun slice(): FloatBuffer {
        val rem = remaining()
        val p = position()
        return FloatBuffer(
            rem, hb, offset + p, readOnly,
            byteView, byteOffset + p * 4, byteOrder, isDirectBuffer
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

    actual override fun compareTo(other: FloatBuffer): Int {
        val n = minOf(this.remaining(), other.remaining())
        val p1 = this.position()
        val p2 = other.position()
        for (i in 0 until n) {
            val cmp = this.get(p1 + i).compareTo(other.get(p2 + i))
            if (cmp != 0) return cmp
        }
        return this.remaining() - other.remaining()
    }

    private fun getAbsolute(index: Int): Float {
        return if (byteView != null) {
            Float.fromBits(readS32(byteView, byteOffset + index * 4, byteOrder))
        } else {
            hb!![offset + index]
        }
    }

    private fun setAbsolute(index: Int, v: Float) {
        if (byteView != null) {
            writeS32(byteView, byteOffset + index * 4, v.toRawBits(), byteOrder)
        } else {
            hb!![offset + index] = v
        }
    }

    private fun copyView(readOnly: Boolean): FloatBuffer {
        return FloatBuffer(capacity(), hb, offset, readOnly, byteView, byteOffset, byteOrder, isDirectBuffer)
    }

    actual companion object {
        actual fun allocate(capacity: Int): FloatBuffer {
            if (capacity < 0) throw IllegalArgumentException("capacity < 0: $capacity")
            return FloatBuffer(capacity, FloatArray(capacity), 0, false, null, 0, ByteOrder.nativeOrder(), false)
        }

        actual fun wrap(array: FloatArray, offset: Int, length: Int): FloatBuffer {
            if (offset < 0 || length < 0 || offset + length > array.size) throw IndexOutOfBoundsException()
            val b = FloatBuffer(array.size, array, 0, false, null, 0, ByteOrder.nativeOrder(), false)
            b.position(offset)
            b.limit(offset + length)
            return b
        }

        actual fun wrap(array: FloatArray): FloatBuffer = wrap(array, 0, array.size)

        internal fun viewOfBytes(
            bytes: ByteArray,
            byteOffset: Int,
            capacity: Int,
            order: ByteOrder,
            readOnly: Boolean,
            isDirect: Boolean
        ): FloatBuffer = FloatBuffer(capacity, null, 0, readOnly, bytes, byteOffset, order, isDirect)
    }
}
