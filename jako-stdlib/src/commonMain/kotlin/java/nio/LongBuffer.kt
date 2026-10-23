@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio

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
