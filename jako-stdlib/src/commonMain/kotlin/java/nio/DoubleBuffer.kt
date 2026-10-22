@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio

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
