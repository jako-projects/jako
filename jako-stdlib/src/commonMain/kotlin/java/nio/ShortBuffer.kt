@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio

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
