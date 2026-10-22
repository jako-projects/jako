@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio

expect open class ByteBuffer : Buffer, Comparable<ByteBuffer> {
    open fun get(): Byte
    open fun put(b: Byte): ByteBuffer
    open fun get(index: Int): Byte
    open fun put(index: Int, b: Byte): ByteBuffer
    open fun get(dst: ByteArray, offset: Int, length: Int): ByteBuffer
    open fun get(dst: ByteArray): ByteBuffer
    open fun put(src: ByteArray, offset: Int, length: Int): ByteBuffer
    open fun put(src: ByteArray): ByteBuffer
    open fun put(src: ByteBuffer): ByteBuffer
    open fun getChar(): Char
    open fun putChar(value: Char): ByteBuffer
    open fun getChar(index: Int): Char
    open fun putChar(index: Int, value: Char): ByteBuffer
    open fun getShort(): Short
    open fun putShort(value: Short): ByteBuffer
    open fun getShort(index: Int): Short
    open fun putShort(index: Int, value: Short): ByteBuffer
    open fun getInt(): Int
    open fun putInt(value: Int): ByteBuffer
    open fun getInt(index: Int): Int
    open fun putInt(index: Int, value: Int): ByteBuffer
    open fun getLong(): Long
    open fun putLong(value: Long): ByteBuffer
    open fun getLong(index: Int): Long
    open fun putLong(index: Int, value: Long): ByteBuffer
    open fun getFloat(): Float
    open fun putFloat(value: Float): ByteBuffer
    open fun getFloat(index: Int): Float
    open fun putFloat(index: Int, value: Float): ByteBuffer
    open fun getDouble(): Double
    open fun putDouble(value: Double): ByteBuffer
    open fun getDouble(index: Int): Double
    open fun putDouble(index: Int, value: Double): ByteBuffer
    open fun order(): ByteOrder
    open fun order(bo: ByteOrder): ByteBuffer
    open fun asReadOnlyBuffer(): ByteBuffer
    open fun duplicate(): ByteBuffer
    open fun slice(): ByteBuffer
    /**
     * Char view of the remaining bytes (2 bytes per char). Shares backing storage
     * with this buffer; position/limit/mark are independent.
     * [Nullability Decision] Java ByteBuffer.asCharBuffer() is non-null.
     */
    open fun asCharBuffer(): CharBuffer
    /**
     * Int view of the remaining bytes (4 bytes per int). Shares backing storage
     * with this buffer; position/limit/mark are independent.
     * [Nullability Decision] Java ByteBuffer.asIntBuffer() is non-null.
     */
    open fun asIntBuffer(): IntBuffer
    /**
     * Long view of the remaining bytes (8 bytes per long). Shares backing storage
     * with this buffer; position/limit/mark are independent.
     * [Nullability Decision] Java ByteBuffer.asLongBuffer() is non-null.
     */
    open fun asLongBuffer(): LongBuffer
    /**
     * Short view of the remaining bytes (2 bytes per short). Shares backing storage
     * with this buffer; position/limit/mark are independent.
     * [Nullability Decision] Java ByteBuffer.asShortBuffer() is non-null.
     */
    open fun asShortBuffer(): ShortBuffer
    /**
     * Float view of the remaining bytes (4 bytes per float). Shares backing storage
     * with this buffer; position/limit/mark are independent.
     * [Nullability Decision] Java ByteBuffer.asFloatBuffer() is non-null.
     */
    open fun asFloatBuffer(): FloatBuffer
    /**
     * Double view of the remaining bytes (8 bytes per double). Shares backing storage
     * with this buffer; position/limit/mark are independent.
     * [Nullability Decision] Java ByteBuffer.asDoubleBuffer() is non-null.
     */
    open fun asDoubleBuffer(): DoubleBuffer
    override fun isReadOnly(): Boolean
    override fun hasArray(): Boolean
    override fun array(): Any
    override fun arrayOffset(): Int
    override fun isDirect(): Boolean

    override val isReadOnly: Boolean
    override val isDirect: Boolean
    override fun compareTo(other: ByteBuffer): Int

    companion object {
        fun allocate(capacity: Int): ByteBuffer
        fun allocateDirect(capacity: Int): ByteBuffer
        fun wrap(array: ByteArray, offset: Int, length: Int): ByteBuffer
        fun wrap(array: ByteArray): ByteBuffer
    }
}
