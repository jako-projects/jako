package android.util

import kotlin.math.abs

actual open class Pair<F, S> actual constructor(
    actual val first: F,
    actual val second: S
) {
    actual override fun equals(other: Any?): Boolean {
        if (other !is Pair<*, *>) return false
        return other.first == first && other.second == second
    }

    actual override fun hashCode(): Int {
        return (first?.hashCode() ?: 0) xor (second?.hashCode() ?: 0)
    }

    actual override fun toString(): String {
        return "Pair{$first $second}"
    }

    actual companion object {
        actual fun <A, B> create(a: A, b: B): Pair<A, B> = Pair(a, b)
    }
}
