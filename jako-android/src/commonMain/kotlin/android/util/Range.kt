@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

/**
 * Immutable class for describing the range of two numeric values.
 */
expect final class Range<T : Comparable<T>>(lower: T, upper: T) {
    val lower: T
    val upper: T

    fun getLower(): T
    fun getUpper(): T
    operator fun contains(value: T): Boolean
    operator fun contains(range: Range<T>): Boolean
    fun clamp(value: T): T
    fun intersect(range: Range<T>): Range<T>
    fun intersect(lower: T, upper: T): Range<T>
    fun extend(range: Range<T>): Range<T>
    fun extend(lower: T, upper: T): Range<T>
    fun extend(value: T): Range<T>
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        fun <T : Comparable<T>> create(lower: T, upper: T): Range<T>
    }
}
