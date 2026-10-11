@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio

expect open class CharBuffer : Buffer, Comparable<CharBuffer>, Appendable {
    open fun get(): Char
    open fun put(c: Char): CharBuffer
    open fun get(index: Int): Char
    open fun put(index: Int, c: Char): CharBuffer
    /** [Nullability Decision] Java CharBuffer.get(char[],int,int) dst is non-null. */
    open fun get(dst: CharArray, offset: Int, length: Int): CharBuffer
    /** [Nullability Decision] Java CharBuffer.get(char[]) dst is non-null. */
    open fun get(dst: CharArray): CharBuffer
    /** [Nullability Decision] Java CharBuffer.put(char[],int,int) src is non-null. */
    open fun put(src: CharArray, offset: Int, length: Int): CharBuffer
    /** [Nullability Decision] Java CharBuffer.put(char[]) src is non-null. */
    open fun put(src: CharArray): CharBuffer
    /** [Nullability Decision] Java CharBuffer.put(CharBuffer) src is non-null. */
    open fun put(src: CharBuffer): CharBuffer
    /** [Nullability Decision] Java CharBuffer.put(String) src is non-null. */
    open fun put(src: String): CharBuffer
    /** [Nullability Decision] Java CharBuffer.put(String,int,int) src is non-null. */
    open fun put(src: String, start: Int, end: Int): CharBuffer
    open fun order(): ByteOrder
    open fun asReadOnlyBuffer(): CharBuffer
    open fun duplicate(): CharBuffer
    open fun slice(): CharBuffer
    open fun charAt(index: Int): Char
    open fun length(): Int
    /** [Nullability Decision] Java CharBuffer.subSequence returns a non-null CharBuffer view. */
    open fun subSequence(start: Int, end: Int): CharBuffer
    override fun isReadOnly(): Boolean
    override fun hasArray(): Boolean
    override fun array(): Any
    override fun arrayOffset(): Int
    override fun isDirect(): Boolean

    override val isReadOnly: Boolean
    override val isDirect: Boolean
    val order: ByteOrder
    override fun compareTo(other: CharBuffer): Int
    override fun append(value: CharSequence?): CharBuffer
    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): CharBuffer
    override fun append(value: Char): CharBuffer

    companion object {
        fun allocate(capacity: Int): CharBuffer
        /** [Nullability Decision] Java CharBuffer.wrap(char[],int,int) array is non-null. */
        fun wrap(array: CharArray, offset: Int, length: Int): CharBuffer
        /** [Nullability Decision] Java CharBuffer.wrap(char[]) array is non-null. */
        fun wrap(array: CharArray): CharBuffer
        /** [Nullability Decision] Java CharBuffer.wrap(CharSequence) csq is non-null. */
        fun wrap(csq: CharSequence): CharBuffer
        /** [Nullability Decision] Java CharBuffer.wrap(CharSequence,int,int) csq is non-null. */
        fun wrap(csq: CharSequence, start: Int, end: Int): CharBuffer
    }
}

expect open class IntBuffer : Buffer, Comparable<IntBuffer> {
    open fun get(): Int
    open fun put(i: Int): IntBuffer
    open fun get(index: Int): Int
    open fun put(index: Int, i: Int): IntBuffer
    /** [Nullability Decision] Java IntBuffer.get(int[],int,int) dst is non-null. */
    open fun get(dst: IntArray, offset: Int, length: Int): IntBuffer
    /** [Nullability Decision] Java IntBuffer.get(int[]) dst is non-null. */
    open fun get(dst: IntArray): IntBuffer
    /** [Nullability Decision] Java IntBuffer.put(int[],int,int) src is non-null. */
    open fun put(src: IntArray, offset: Int, length: Int): IntBuffer
    /** [Nullability Decision] Java IntBuffer.put(int[]) src is non-null. */
    open fun put(src: IntArray): IntBuffer
    /** [Nullability Decision] Java IntBuffer.put(IntBuffer) src is non-null. */
    open fun put(src: IntBuffer): IntBuffer
    open fun order(): ByteOrder
    open fun asReadOnlyBuffer(): IntBuffer
    open fun duplicate(): IntBuffer
    open fun slice(): IntBuffer
    override fun isReadOnly(): Boolean
    override fun hasArray(): Boolean
    override fun array(): Any
    override fun arrayOffset(): Int
    override fun isDirect(): Boolean

    override val isReadOnly: Boolean
    override val isDirect: Boolean
    val order: ByteOrder
    override fun compareTo(other: IntBuffer): Int

    companion object {
        fun allocate(capacity: Int): IntBuffer
        /** [Nullability Decision] Java IntBuffer.wrap(int[],int,int) array is non-null. */
        fun wrap(array: IntArray, offset: Int, length: Int): IntBuffer
        /** [Nullability Decision] Java IntBuffer.wrap(int[]) array is non-null. */
        fun wrap(array: IntArray): IntBuffer
    }
}

expect open class LongBuffer : Buffer, Comparable<LongBuffer> {
    open fun get(): Long
    open fun put(l: Long): LongBuffer
    open fun get(index: Int): Long
    open fun put(index: Int, l: Long): LongBuffer
    /** [Nullability Decision] Java LongBuffer.get(long[],int,int) dst is non-null. */
    open fun get(dst: LongArray, offset: Int, length: Int): LongBuffer
    /** [Nullability Decision] Java LongBuffer.get(long[]) dst is non-null. */
    open fun get(dst: LongArray): LongBuffer
    /** [Nullability Decision] Java LongBuffer.put(long[],int,int) src is non-null. */
    open fun put(src: LongArray, offset: Int, length: Int): LongBuffer
    /** [Nullability Decision] Java LongBuffer.put(long[]) src is non-null. */
    open fun put(src: LongArray): LongBuffer
    /** [Nullability Decision] Java LongBuffer.put(LongBuffer) src is non-null. */
    open fun put(src: LongBuffer): LongBuffer
    open fun order(): ByteOrder
    open fun asReadOnlyBuffer(): LongBuffer
    open fun duplicate(): LongBuffer
    open fun slice(): LongBuffer
    override fun isReadOnly(): Boolean
    override fun hasArray(): Boolean
    override fun array(): Any
    override fun arrayOffset(): Int
    override fun isDirect(): Boolean

    override val isReadOnly: Boolean
    override val isDirect: Boolean
    val order: ByteOrder
    override fun compareTo(other: LongBuffer): Int

    companion object {
        fun allocate(capacity: Int): LongBuffer
        /** [Nullability Decision] Java LongBuffer.wrap(long[],int,int) array is non-null. */
        fun wrap(array: LongArray, offset: Int, length: Int): LongBuffer
        /** [Nullability Decision] Java LongBuffer.wrap(long[]) array is non-null. */
        fun wrap(array: LongArray): LongBuffer
    }
}

expect open class ShortBuffer : Buffer, Comparable<ShortBuffer> {
    open fun get(): Short
    open fun put(s: Short): ShortBuffer
    open fun get(index: Int): Short
    open fun put(index: Int, s: Short): ShortBuffer
    /** [Nullability Decision] Java ShortBuffer.get(short[],int,int) dst is non-null. */
    open fun get(dst: ShortArray, offset: Int, length: Int): ShortBuffer
    /** [Nullability Decision] Java ShortBuffer.get(short[]) dst is non-null. */
    open fun get(dst: ShortArray): ShortBuffer
    /** [Nullability Decision] Java ShortBuffer.put(short[],int,int) src is non-null. */
    open fun put(src: ShortArray, offset: Int, length: Int): ShortBuffer
    /** [Nullability Decision] Java ShortBuffer.put(short[]) src is non-null. */
    open fun put(src: ShortArray): ShortBuffer
    /** [Nullability Decision] Java ShortBuffer.put(ShortBuffer) src is non-null. */
    open fun put(src: ShortBuffer): ShortBuffer
    open fun order(): ByteOrder
    open fun asReadOnlyBuffer(): ShortBuffer
    open fun duplicate(): ShortBuffer
    open fun slice(): ShortBuffer
    override fun isReadOnly(): Boolean
    override fun hasArray(): Boolean
    override fun array(): Any
    override fun arrayOffset(): Int
    override fun isDirect(): Boolean

    override val isReadOnly: Boolean
    override val isDirect: Boolean
    val order: ByteOrder
    override fun compareTo(other: ShortBuffer): Int

    companion object {
        fun allocate(capacity: Int): ShortBuffer
        /** [Nullability Decision] Java ShortBuffer.wrap(short[],int,int) array is non-null. */
        fun wrap(array: ShortArray, offset: Int, length: Int): ShortBuffer
        /** [Nullability Decision] Java ShortBuffer.wrap(short[]) array is non-null. */
        fun wrap(array: ShortArray): ShortBuffer
    }
}

expect open class FloatBuffer : Buffer, Comparable<FloatBuffer> {
    open fun get(): Float
    open fun put(f: Float): FloatBuffer
    open fun get(index: Int): Float
    open fun put(index: Int, f: Float): FloatBuffer
    /** [Nullability Decision] Java FloatBuffer.get(float[],int,int) dst is non-null. */
    open fun get(dst: FloatArray, offset: Int, length: Int): FloatBuffer
    /** [Nullability Decision] Java FloatBuffer.get(float[]) dst is non-null. */
    open fun get(dst: FloatArray): FloatBuffer
    /** [Nullability Decision] Java FloatBuffer.put(float[],int,int) src is non-null. */
    open fun put(src: FloatArray, offset: Int, length: Int): FloatBuffer
    /** [Nullability Decision] Java FloatBuffer.put(float[]) src is non-null. */
    open fun put(src: FloatArray): FloatBuffer
    /** [Nullability Decision] Java FloatBuffer.put(FloatBuffer) src is non-null. */
    open fun put(src: FloatBuffer): FloatBuffer
    open fun order(): ByteOrder
    open fun asReadOnlyBuffer(): FloatBuffer
    open fun duplicate(): FloatBuffer
    open fun slice(): FloatBuffer
    override fun isReadOnly(): Boolean
    override fun hasArray(): Boolean
    override fun array(): Any
    override fun arrayOffset(): Int
    override fun isDirect(): Boolean

    override val isReadOnly: Boolean
    override val isDirect: Boolean
    val order: ByteOrder
    override fun compareTo(other: FloatBuffer): Int

    companion object {
        fun allocate(capacity: Int): FloatBuffer
        /** [Nullability Decision] Java FloatBuffer.wrap(float[],int,int) array is non-null. */
        fun wrap(array: FloatArray, offset: Int, length: Int): FloatBuffer
        /** [Nullability Decision] Java FloatBuffer.wrap(float[]) array is non-null. */
        fun wrap(array: FloatArray): FloatBuffer
    }
}

expect open class DoubleBuffer : Buffer, Comparable<DoubleBuffer> {
    open fun get(): Double
    open fun put(d: Double): DoubleBuffer
    open fun get(index: Int): Double
    open fun put(index: Int, d: Double): DoubleBuffer
    /** [Nullability Decision] Java DoubleBuffer.get(double[],int,int) dst is non-null. */
    open fun get(dst: DoubleArray, offset: Int, length: Int): DoubleBuffer
    /** [Nullability Decision] Java DoubleBuffer.get(double[]) dst is non-null. */
    open fun get(dst: DoubleArray): DoubleBuffer
    /** [Nullability Decision] Java DoubleBuffer.put(double[],int,int) src is non-null. */
    open fun put(src: DoubleArray, offset: Int, length: Int): DoubleBuffer
    /** [Nullability Decision] Java DoubleBuffer.put(double[]) src is non-null. */
    open fun put(src: DoubleArray): DoubleBuffer
    /** [Nullability Decision] Java DoubleBuffer.put(DoubleBuffer) src is non-null. */
    open fun put(src: DoubleBuffer): DoubleBuffer
    open fun order(): ByteOrder
    open fun asReadOnlyBuffer(): DoubleBuffer
    open fun duplicate(): DoubleBuffer
    open fun slice(): DoubleBuffer
    override fun isReadOnly(): Boolean
    override fun hasArray(): Boolean
    override fun array(): Any
    override fun arrayOffset(): Int
    override fun isDirect(): Boolean

    override val isReadOnly: Boolean
    override val isDirect: Boolean
    val order: ByteOrder
    override fun compareTo(other: DoubleBuffer): Int

    companion object {
        fun allocate(capacity: Int): DoubleBuffer
        /** [Nullability Decision] Java DoubleBuffer.wrap(double[],int,int) array is non-null. */
        fun wrap(array: DoubleArray, offset: Int, length: Int): DoubleBuffer
        /** [Nullability Decision] Java DoubleBuffer.wrap(double[]) array is non-null. */
        fun wrap(array: DoubleArray): DoubleBuffer
    }
}
