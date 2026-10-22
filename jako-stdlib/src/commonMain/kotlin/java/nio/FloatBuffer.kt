@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio

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
