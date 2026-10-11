package java.nio

actual open class ByteOrder private constructor(private val nameStr: String) {
    override fun toString(): String = nameStr

    actual companion object {
        actual val BIG_ENDIAN: ByteOrder = ByteOrder("BIG_ENDIAN")
        actual val LITTLE_ENDIAN: ByteOrder = ByteOrder("LITTLE_ENDIAN")
        actual fun nativeOrder(): ByteOrder = LITTLE_ENDIAN
    }
}

actual abstract class Buffer(
    private var cap: Int,
    private var lim: Int
) {
    private var pos: Int = 0
    private var mrk: Int = -1

    actual open fun capacity(): Int = cap
    actual open fun position(): Int = pos

    actual open fun position(newPosition: Int): Buffer {
        if (newPosition < 0 || newPosition > lim) throw IllegalArgumentException("Bad position: $newPosition")
        pos = newPosition
        if (mrk > pos) mrk = -1
        return this
    }

    actual open fun limit(): Int = lim

    actual open fun limit(newLimit: Int): Buffer {
        if (newLimit < 0 || newLimit > cap) throw IllegalArgumentException("Bad limit: $newLimit")
        lim = newLimit
        if (pos > lim) pos = lim
        if (mrk > lim) mrk = -1
        return this
    }

    actual open fun mark(): Buffer {
        mrk = pos
        return this
    }

    actual open fun reset(): Buffer {
        if (mrk < 0) throw IllegalStateException("Mark not set")
        pos = mrk
        return this
    }

    actual open fun clear(): Buffer {
        pos = 0
        lim = cap
        mrk = -1
        return this
    }

    actual open fun flip(): Buffer {
        lim = pos
        pos = 0
        mrk = -1
        return this
    }

    actual open fun rewind(): Buffer {
        pos = 0
        mrk = -1
        return this
    }

    actual open fun remaining(): Int = lim - pos

    actual open fun hasRemaining(): Boolean = pos < lim

    actual abstract fun isReadOnly(): Boolean
    actual abstract fun hasArray(): Boolean
    actual abstract fun array(): Any
    actual abstract fun arrayOffset(): Int
    actual abstract fun isDirect(): Boolean

    actual abstract val isReadOnly: Boolean
    actual abstract val isDirect: Boolean
}

actual open class ByteBuffer internal constructor(
    capacity: Int,
    internal val hb: ByteArray?,
    internal val offset: Int,
    internal val isDirectBuffer: Boolean,
    internal val readOnly: Boolean
) : Buffer(capacity, capacity), Comparable<ByteBuffer> {

    private var byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN
    internal val arrayData: ByteArray = hb ?: ByteArray(capacity)

    actual open fun get(): Byte {
        val p = position()
        if (p >= limit()) throw IndexOutOfBoundsException("Buffer overflow")
        val b = arrayData[offset + p]
        position(p + 1)
        return b
    }

    actual open fun put(b: Byte): ByteBuffer {
        if (isReadOnly()) throw IllegalStateException("Read-only buffer")
        val p = position()
        if (p >= limit()) throw IndexOutOfBoundsException("Buffer overflow")
        arrayData[offset + p] = b
        position(p + 1)
        return this
    }

    actual open fun get(index: Int): Byte {
        if (index < 0 || index >= limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        return arrayData[offset + index]
    }

    actual open fun put(index: Int, b: Byte): ByteBuffer {
        if (isReadOnly()) throw IllegalStateException("Read-only buffer")
        if (index < 0 || index >= limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        arrayData[offset + index] = b
        return this
    }

    actual open fun get(dst: ByteArray, offset: Int, length: Int): ByteBuffer {
        if (remaining() < length) throw IndexOutOfBoundsException("Buffer underflow")
        val p = position()
        for (i in 0 until length) {
            dst[offset + i] = arrayData[this.offset + p + i]
        }
        position(p + length)
        return this
    }

    actual open fun get(dst: ByteArray): ByteBuffer = get(dst, 0, dst.size)

    actual open fun put(src: ByteArray, offset: Int, length: Int): ByteBuffer {
        if (isReadOnly()) throw IllegalStateException("Read-only buffer")
        if (remaining() < length) throw IndexOutOfBoundsException("Buffer overflow")
        val p = position()
        for (i in 0 until length) {
            arrayData[this.offset + p + i] = src[offset + i]
        }
        position(p + length)
        return this
    }

    actual open fun put(src: ByteArray): ByteBuffer = put(src, 0, src.size)

    actual open fun put(src: ByteBuffer): ByteBuffer {
        if (src === this) throw IllegalArgumentException("Cannot put buffer into itself")
        if (isReadOnly()) throw IllegalStateException("Read-only buffer")
        val n = src.remaining()
        if (n > remaining()) throw IndexOutOfBoundsException("Buffer overflow")
        for (i in 0 until n) {
            put(src.get())
        }
        return this
    }

    actual open fun getChar(): Char = getShort().toInt().toChar()
    actual open fun putChar(value: Char): ByteBuffer = putShort(value.code.toShort())
    actual open fun getChar(index: Int): Char = getShort(index).toInt().toChar()
    actual open fun putChar(index: Int, value: Char): ByteBuffer = putShort(index, value.code.toShort())

    actual open fun getShort(): Short {
        val p = position()
        if (remaining() < 2) throw IndexOutOfBoundsException("Buffer underflow")
        val v = readU16(arrayData, offset + p, byteOrder).toShort()
        position(p + 2)
        return v
    }

    actual open fun putShort(value: Short): ByteBuffer {
        if (isReadOnly()) throw IllegalStateException("Read-only buffer")
        val p = position()
        if (remaining() < 2) throw IndexOutOfBoundsException("Buffer overflow")
        writeU16(arrayData, offset + p, value.toInt(), byteOrder)
        position(p + 2)
        return this
    }

    actual open fun getShort(index: Int): Short {
        if (index < 0 || index + 2 > limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        return readU16(arrayData, offset + index, byteOrder).toShort()
    }

    actual open fun putShort(index: Int, value: Short): ByteBuffer {
        if (isReadOnly()) throw IllegalStateException("Read-only buffer")
        if (index < 0 || index + 2 > limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        writeU16(arrayData, offset + index, value.toInt(), byteOrder)
        return this
    }

    actual open fun getInt(): Int {
        val p = position()
        if (remaining() < 4) throw IndexOutOfBoundsException("Buffer underflow")
        val v = readS32(arrayData, offset + p, byteOrder)
        position(p + 4)
        return v
    }

    actual open fun putInt(value: Int): ByteBuffer {
        if (isReadOnly()) throw IllegalStateException("Read-only buffer")
        val p = position()
        if (remaining() < 4) throw IndexOutOfBoundsException("Buffer overflow")
        writeS32(arrayData, offset + p, value, byteOrder)
        position(p + 4)
        return this
    }

    actual open fun getInt(index: Int): Int {
        if (index < 0 || index + 4 > limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        return readS32(arrayData, offset + index, byteOrder)
    }

    actual open fun putInt(index: Int, value: Int): ByteBuffer {
        if (isReadOnly()) throw IllegalStateException("Read-only buffer")
        if (index < 0 || index + 4 > limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        writeS32(arrayData, offset + index, value, byteOrder)
        return this
    }

    actual open fun getLong(): Long {
        val p = position()
        if (remaining() < 8) throw IndexOutOfBoundsException("Buffer underflow")
        val v = readS64(arrayData, offset + p, byteOrder)
        position(p + 8)
        return v
    }

    actual open fun putLong(value: Long): ByteBuffer {
        if (isReadOnly()) throw IllegalStateException("Read-only buffer")
        val p = position()
        if (remaining() < 8) throw IndexOutOfBoundsException("Buffer overflow")
        writeS64(arrayData, offset + p, value, byteOrder)
        position(p + 8)
        return this
    }

    actual open fun getLong(index: Int): Long {
        if (index < 0 || index + 8 > limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        return readS64(arrayData, offset + index, byteOrder)
    }

    actual open fun putLong(index: Int, value: Long): ByteBuffer {
        if (isReadOnly()) throw IllegalStateException("Read-only buffer")
        if (index < 0 || index + 8 > limit()) throw IndexOutOfBoundsException("Index out of bounds: $index")
        writeS64(arrayData, offset + index, value, byteOrder)
        return this
    }

    actual open fun getFloat(): Float = Float.fromBits(getInt())
    actual open fun putFloat(value: Float): ByteBuffer = putInt(value.toRawBits())
    actual open fun getFloat(index: Int): Float = Float.fromBits(getInt(index))
    actual open fun putFloat(index: Int, value: Float): ByteBuffer = putInt(index, value.toRawBits())

    actual open fun getDouble(): Double = Double.fromBits(getLong())
    actual open fun putDouble(value: Double): ByteBuffer = putLong(value.toRawBits())
    actual open fun getDouble(index: Int): Double = Double.fromBits(getLong(index))
    actual open fun putDouble(index: Int, value: Double): ByteBuffer = putLong(index, value.toRawBits())

    actual open fun order(): ByteOrder = byteOrder
    actual open fun order(bo: ByteOrder): ByteBuffer {
        byteOrder = bo
        return this
    }

    actual open fun asCharBuffer(): CharBuffer {
        val rem = remaining()
        return CharBuffer.viewOfBytes(arrayData, offset + position(), rem / 2, byteOrder, readOnly, isDirectBuffer)
    }

    actual open fun asIntBuffer(): IntBuffer {
        val rem = remaining()
        return IntBuffer.viewOfBytes(arrayData, offset + position(), rem / 4, byteOrder, readOnly, isDirectBuffer)
    }

    actual open fun asLongBuffer(): LongBuffer {
        val rem = remaining()
        return LongBuffer.viewOfBytes(arrayData, offset + position(), rem / 8, byteOrder, readOnly, isDirectBuffer)
    }

    actual open fun asShortBuffer(): ShortBuffer {
        val rem = remaining()
        return ShortBuffer.viewOfBytes(arrayData, offset + position(), rem / 2, byteOrder, readOnly, isDirectBuffer)
    }

    actual open fun asFloatBuffer(): FloatBuffer {
        val rem = remaining()
        return FloatBuffer.viewOfBytes(arrayData, offset + position(), rem / 4, byteOrder, readOnly, isDirectBuffer)
    }

    actual open fun asDoubleBuffer(): DoubleBuffer {
        val rem = remaining()
        return DoubleBuffer.viewOfBytes(arrayData, offset + position(), rem / 8, byteOrder, readOnly, isDirectBuffer)
    }

    actual open fun asReadOnlyBuffer(): ByteBuffer {
        val b = ByteBuffer(capacity(), hb, offset, isDirectBuffer, true)
        b.limit(limit())
        b.position(position())
        return b
    }

    actual open fun duplicate(): ByteBuffer {
        val b = ByteBuffer(capacity(), hb, offset, isDirectBuffer, readOnly)
        b.limit(limit())
        b.position(position())
        return b
    }

    actual open fun slice(): ByteBuffer {
        val rem = remaining()
        val b = ByteBuffer(rem, hb, offset + position(), isDirectBuffer, readOnly)
        return b
    }

    actual override fun isReadOnly(): Boolean = readOnly
    actual override fun hasArray(): Boolean = hb != null
    actual override fun array(): Any = hb ?: throw UnsupportedOperationException()
    actual override fun arrayOffset(): Int = offset
    actual override fun isDirect(): Boolean = isDirectBuffer

    actual override val isReadOnly: Boolean get() = isReadOnly()
    actual override val isDirect: Boolean get() = isDirect()

    actual override fun compareTo(other: ByteBuffer): Int {
        val n = minOf(this.remaining(), other.remaining())
        val p1 = this.position()
        val p2 = other.position()
        for (i in 0 until n) {
            val cmp = this.get(p1 + i).compareTo(other.get(p2 + i))
            if (cmp != 0) return cmp
        }
        return this.remaining() - other.remaining()
    }

    actual companion object {
        actual fun allocate(capacity: Int): ByteBuffer = ByteBuffer(capacity, ByteArray(capacity), 0, false, false)
        actual fun allocateDirect(capacity: Int): ByteBuffer = ByteBuffer(capacity, ByteArray(capacity), 0, true, false)
        actual fun wrap(array: ByteArray, offset: Int, length: Int): ByteBuffer {
            val b = ByteBuffer(array.size, array, 0, false, false)
            b.position(offset)
            b.limit(offset + length)
            return b
        }
        actual fun wrap(array: ByteArray): ByteBuffer = wrap(array, 0, array.size)
    }
}

actual open class BufferOverflowException actual constructor() : RuntimeException()

actual open class BufferUnderflowException actual constructor() : RuntimeException()

actual open class ReadOnlyBufferException actual constructor() : UnsupportedOperationException()
