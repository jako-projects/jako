@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio

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
