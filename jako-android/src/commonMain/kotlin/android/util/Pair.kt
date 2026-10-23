@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

/**
 * Container to ease passing around a tuple of two objects.
 *
 * [Nullability Decision]:
 * - Type parameters `F` and `S` have upper bound `Any?` to support nullable elements matching AOSP `Pair<F, S>`.
 */
expect open class Pair<F, S>(first: F, second: S) {
    val first: F
    val second: S

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        fun <A, B> create(a: A, b: B): Pair<A, B>
    }
}
