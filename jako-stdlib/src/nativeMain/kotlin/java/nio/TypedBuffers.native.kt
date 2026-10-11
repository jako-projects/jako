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

actual open class CharBuffer internal constructor(
    capacity: Int,
    private val hb: CharArray?,
    private val offset: Int,
    private val readOnly: Boolean,
    private val byteView: ByteArray?,
    private val byteOffset: Int,
    private val byteOrder: ByteOrder,
    private val isDirectBuffer: Boolean
) : Buffer(capacity, capacity), Comparable<CharBuffer>, Appendable {

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

actual open class ShortBuffer internal constructor(
    capacity: Int,
    private val hb: ShortArray?,
    private val offset: Int,
    private val readOnly: Boolean,
    private val byteView: ByteArray?,
    private val byteOffset: Int,
    private val byteOrder: ByteOrder,
    private val isDirectBuffer: Boolean
) : Buffer(capacity, capacity), Comparable<ShortBuffer> {

    actual open fun get(): Short {
        val p = position()
        if (p >= limit()) throw BufferUnderflowException()
        val v = getAbsolute(p)
        position(p + 1)
        return v
    }

    actual open fun put(s: Short): ShortBuffer {
        if (readOnly) throw ReadOnlyBufferException()
        val p = position()
        if (p >= limit()) throw BufferOverflowException()
        setAbsolute(p, s)
        position(p + 1)
        return this
    }

    actual open fun get(index: Int): Short {
        if (index < 0 || index >= limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        return getAbsolute(index)
    }

    actual open fun put(index: Int, s: Short): ShortBuffer {
        if (readOnly) throw ReadOnlyBufferException()
        if (index < 0 || index >= limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        setAbsolute(index, s)
        return this
    }

    actual open fun get(dst: ShortArray, offset: Int, length: Int): ShortBuffer {
        if (offset < 0 || length < 0 || offset + length > dst.size) throw IndexOutOfBoundsException()
        if (remaining() < length) throw BufferUnderflowException()
        val p = position()
        for (i in 0 until length) {
            dst[offset + i] = getAbsolute(p + i)
        }
        position(p + length)
        return this
    }

    actual open fun get(dst: ShortArray): ShortBuffer = get(dst, 0, dst.size)

    actual open fun put(src: ShortArray, offset: Int, length: Int): ShortBuffer {
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

    actual open fun put(src: ShortArray): ShortBuffer = put(src, 0, src.size)

    actual open fun put(src: ShortBuffer): ShortBuffer {
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

    actual open fun asReadOnlyBuffer(): ShortBuffer {
        val b = copyView(true)
        b.limit(limit())
        b.position(position())
        return b
    }

    actual open fun duplicate(): ShortBuffer {
        val b = copyView(readOnly)
        b.limit(limit())
        b.position(position())
        return b
    }

    actual open fun slice(): ShortBuffer {
        val rem = remaining()
        val p = position()
        return ShortBuffer(
            rem, hb, offset + p, readOnly,
            byteView, byteOffset + p * 2, byteOrder, isDirectBuffer
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

    actual override fun compareTo(other: ShortBuffer): Int {
        val n = minOf(this.remaining(), other.remaining())
        val p1 = this.position()
        val p2 = other.position()
        for (i in 0 until n) {
            val cmp = this.get(p1 + i).compareTo(other.get(p2 + i))
            if (cmp != 0) return cmp
        }
        return this.remaining() - other.remaining()
    }

    private fun getAbsolute(index: Int): Short {
        return if (byteView != null) {
            readU16(byteView, byteOffset + index * 2, byteOrder).toShort()
        } else {
            hb!![offset + index]
        }
    }

    private fun setAbsolute(index: Int, v: Short) {
        if (byteView != null) {
            writeU16(byteView, byteOffset + index * 2, v.toInt(), byteOrder)
        } else {
            hb!![offset + index] = v
        }
    }

    private fun copyView(readOnly: Boolean): ShortBuffer {
        return ShortBuffer(capacity(), hb, offset, readOnly, byteView, byteOffset, byteOrder, isDirectBuffer)
    }

    actual companion object {
        actual fun allocate(capacity: Int): ShortBuffer {
            if (capacity < 0) throw IllegalArgumentException("capacity < 0: $capacity")
            return ShortBuffer(capacity, ShortArray(capacity), 0, false, null, 0, ByteOrder.nativeOrder(), false)
        }

        actual fun wrap(array: ShortArray, offset: Int, length: Int): ShortBuffer {
            if (offset < 0 || length < 0 || offset + length > array.size) throw IndexOutOfBoundsException()
            val b = ShortBuffer(array.size, array, 0, false, null, 0, ByteOrder.nativeOrder(), false)
            b.position(offset)
            b.limit(offset + length)
            return b
        }

        actual fun wrap(array: ShortArray): ShortBuffer = wrap(array, 0, array.size)

        internal fun viewOfBytes(
            bytes: ByteArray,
            byteOffset: Int,
            capacity: Int,
            order: ByteOrder,
            readOnly: Boolean,
            isDirect: Boolean
        ): ShortBuffer = ShortBuffer(capacity, null, 0, readOnly, bytes, byteOffset, order, isDirect)
    }
}

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

actual open class DoubleBuffer internal constructor(
    capacity: Int,
    private val hb: DoubleArray?,
    private val offset: Int,
    private val readOnly: Boolean,
    private val byteView: ByteArray?,
    private val byteOffset: Int,
    private val byteOrder: ByteOrder,
    private val isDirectBuffer: Boolean
) : Buffer(capacity, capacity), Comparable<DoubleBuffer> {

    actual open fun get(): Double {
        val p = position()
        if (p >= limit()) throw BufferUnderflowException()
        val v = getAbsolute(p)
        position(p + 1)
        return v
    }

    actual open fun put(d: Double): DoubleBuffer {
        if (readOnly) throw ReadOnlyBufferException()
        val p = position()
        if (p >= limit()) throw BufferOverflowException()
        setAbsolute(p, d)
        position(p + 1)
        return this
    }

    actual open fun get(index: Int): Double {
        if (index < 0 || index >= limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        return getAbsolute(index)
    }

    actual open fun put(index: Int, d: Double): DoubleBuffer {
        if (readOnly) throw ReadOnlyBufferException()
        if (index < 0 || index >= limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        setAbsolute(index, d)
        return this
    }

    actual open fun get(dst: DoubleArray, offset: Int, length: Int): DoubleBuffer {
        if (offset < 0 || length < 0 || offset + length > dst.size) throw IndexOutOfBoundsException()
        if (remaining() < length) throw BufferUnderflowException()
        val p = position()
        for (i in 0 until length) {
            dst[offset + i] = getAbsolute(p + i)
        }
        position(p + length)
        return this
    }

    actual open fun get(dst: DoubleArray): DoubleBuffer = get(dst, 0, dst.size)

    actual open fun put(src: DoubleArray, offset: Int, length: Int): DoubleBuffer {
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

    actual open fun put(src: DoubleArray): DoubleBuffer = put(src, 0, src.size)

    actual open fun put(src: DoubleBuffer): DoubleBuffer {
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

    actual open fun asReadOnlyBuffer(): DoubleBuffer {
        val b = copyView(true)
        b.limit(limit())
        b.position(position())
        return b
    }

    actual open fun duplicate(): DoubleBuffer {
        val b = copyView(readOnly)
        b.limit(limit())
        b.position(position())
        return b
    }

    actual open fun slice(): DoubleBuffer {
        val rem = remaining()
        val p = position()
        return DoubleBuffer(
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

    actual override fun compareTo(other: DoubleBuffer): Int {
        val n = minOf(this.remaining(), other.remaining())
        val p1 = this.position()
        val p2 = other.position()
        for (i in 0 until n) {
            val cmp = this.get(p1 + i).compareTo(other.get(p2 + i))
            if (cmp != 0) return cmp
        }
        return this.remaining() - other.remaining()
    }

    private fun getAbsolute(index: Int): Double {
        return if (byteView != null) {
            Double.fromBits(readS64(byteView, byteOffset + index * 8, byteOrder))
        } else {
            hb!![offset + index]
        }
    }

    private fun setAbsolute(index: Int, v: Double) {
        if (byteView != null) {
            writeS64(byteView, byteOffset + index * 8, v.toRawBits(), byteOrder)
        } else {
            hb!![offset + index] = v
        }
    }

    private fun copyView(readOnly: Boolean): DoubleBuffer {
        return DoubleBuffer(capacity(), hb, offset, readOnly, byteView, byteOffset, byteOrder, isDirectBuffer)
    }

    actual companion object {
        actual fun allocate(capacity: Int): DoubleBuffer {
            if (capacity < 0) throw IllegalArgumentException("capacity < 0: $capacity")
            return DoubleBuffer(capacity, DoubleArray(capacity), 0, false, null, 0, ByteOrder.nativeOrder(), false)
        }

        actual fun wrap(array: DoubleArray, offset: Int, length: Int): DoubleBuffer {
            if (offset < 0 || length < 0 || offset + length > array.size) throw IndexOutOfBoundsException()
            val b = DoubleBuffer(array.size, array, 0, false, null, 0, ByteOrder.nativeOrder(), false)
            b.position(offset)
            b.limit(offset + length)
            return b
        }

        actual fun wrap(array: DoubleArray): DoubleBuffer = wrap(array, 0, array.size)

        internal fun viewOfBytes(
            bytes: ByteArray,
            byteOffset: Int,
            capacity: Int,
            order: ByteOrder,
            readOnly: Boolean,
            isDirect: Boolean
        ): DoubleBuffer = DoubleBuffer(capacity, null, 0, readOnly, bytes, byteOffset, order, isDirect)
    }
}
