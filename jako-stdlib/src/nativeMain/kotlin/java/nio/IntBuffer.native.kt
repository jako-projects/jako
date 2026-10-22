package java.nio

internal fun readS32(bytes: ByteArray, off: Int, order: ByteOrder): Int {
    val b0 = bytes[off].toInt() and 0xff
    val b1 = bytes[off + 1].toInt() and 0xff
    val b2 = bytes[off + 2].toInt() and 0xff
    val b3 = bytes[off + 3].toInt() and 0xff
    return if (order == ByteOrder.BIG_ENDIAN) {
        (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3
    } else {
        (b3 shl 24) or (b2 shl 16) or (b1 shl 8) or b0
    }
}

internal fun writeS32(bytes: ByteArray, off: Int, value: Int, order: ByteOrder) {
    if (order == ByteOrder.BIG_ENDIAN) {
        bytes[off] = (value ushr 24).toByte()
        bytes[off + 1] = (value ushr 16).toByte()
        bytes[off + 2] = (value ushr 8).toByte()
        bytes[off + 3] = value.toByte()
    } else {
        bytes[off] = value.toByte()
        bytes[off + 1] = (value ushr 8).toByte()
        bytes[off + 2] = (value ushr 16).toByte()
        bytes[off + 3] = (value ushr 24).toByte()
    }
}

actual open class IntBuffer internal constructor(
    capacity: Int,
    private val hb: IntArray?,
    private val offset: Int,
    private val readOnly: Boolean,
    private val byteView: ByteArray?,
    private val byteOffset: Int,
    private val byteOrder: ByteOrder,
    private val isDirectBuffer: Boolean
) : Buffer(capacity, capacity), Comparable<IntBuffer> {

    actual open fun get(): Int {
        val p = position()
        if (p >= limit()) throw BufferUnderflowException()
        val v = getAbsolute(p)
        position(p + 1)
        return v
    }

    actual open fun put(i: Int): IntBuffer {
        if (readOnly) throw ReadOnlyBufferException()
        val p = position()
        if (p >= limit()) throw BufferOverflowException()
        setAbsolute(p, i)
        position(p + 1)
        return this
    }

    actual open fun get(index: Int): Int {
        if (index < 0 || index >= limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        return getAbsolute(index)
    }

    actual open fun put(index: Int, i: Int): IntBuffer {
        if (readOnly) throw ReadOnlyBufferException()
        if (index < 0 || index >= limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        setAbsolute(index, i)
        return this
    }

    actual open fun get(dst: IntArray, offset: Int, length: Int): IntBuffer {
        if (offset < 0 || length < 0 || offset + length > dst.size) throw IndexOutOfBoundsException()
        if (remaining() < length) throw BufferUnderflowException()
        val p = position()
        for (i in 0 until length) {
            dst[offset + i] = getAbsolute(p + i)
        }
        position(p + length)
        return this
    }

    actual open fun get(dst: IntArray): IntBuffer = get(dst, 0, dst.size)

    actual open fun put(src: IntArray, offset: Int, length: Int): IntBuffer {
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

    actual open fun put(src: IntArray): IntBuffer = put(src, 0, src.size)

    actual open fun put(src: IntBuffer): IntBuffer {
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

    actual open fun asReadOnlyBuffer(): IntBuffer {
        val b = copyView(true)
        b.limit(limit())
        b.position(position())
        return b
    }

    actual open fun duplicate(): IntBuffer {
        val b = copyView(readOnly)
        b.limit(limit())
        b.position(position())
        return b
    }

    actual open fun slice(): IntBuffer {
        val rem = remaining()
        val p = position()
        return IntBuffer(
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

    actual override fun compareTo(other: IntBuffer): Int {
        val n = minOf(this.remaining(), other.remaining())
        val p1 = this.position()
        val p2 = other.position()
        for (i in 0 until n) {
            val cmp = this.get(p1 + i).compareTo(other.get(p2 + i))
            if (cmp != 0) return cmp
        }
        return this.remaining() - other.remaining()
    }

    private fun getAbsolute(index: Int): Int {
        return if (byteView != null) {
            readS32(byteView, byteOffset + index * 4, byteOrder)
        } else {
            hb!![offset + index]
        }
    }

    private fun setAbsolute(index: Int, v: Int) {
        if (byteView != null) {
            writeS32(byteView, byteOffset + index * 4, v, byteOrder)
        } else {
            hb!![offset + index] = v
        }
    }

    private fun copyView(readOnly: Boolean): IntBuffer {
        return IntBuffer(capacity(), hb, offset, readOnly, byteView, byteOffset, byteOrder, isDirectBuffer)
    }

    actual companion object {
        actual fun allocate(capacity: Int): IntBuffer {
            if (capacity < 0) throw IllegalArgumentException("capacity < 0: $capacity")
            return IntBuffer(capacity, IntArray(capacity), 0, false, null, 0, ByteOrder.nativeOrder(), false)
        }

        actual fun wrap(array: IntArray, offset: Int, length: Int): IntBuffer {
            if (offset < 0 || length < 0 || offset + length > array.size) throw IndexOutOfBoundsException()
            val b = IntBuffer(array.size, array, 0, false, null, 0, ByteOrder.nativeOrder(), false)
            b.position(offset)
            b.limit(offset + length)
            return b
        }

        actual fun wrap(array: IntArray): IntBuffer = wrap(array, 0, array.size)

        internal fun viewOfBytes(
            bytes: ByteArray,
            byteOffset: Int,
            capacity: Int,
            order: ByteOrder,
            readOnly: Boolean,
            isDirect: Boolean
        ): IntBuffer = IntBuffer(capacity, null, 0, readOnly, bytes, byteOffset, order, isDirect)
    }
}
