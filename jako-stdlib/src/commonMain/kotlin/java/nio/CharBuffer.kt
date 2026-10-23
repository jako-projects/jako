@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio

expect open class CharBuffer : Buffer, Comparable<CharBuffer>, Appendable, java.lang.Readable {
    override fun read(target: CharBuffer): Int
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
