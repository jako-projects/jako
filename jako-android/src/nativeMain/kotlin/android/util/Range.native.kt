package android.util

import kotlin.math.abs

actual final class Range<T : Comparable<T>> actual constructor(
    actual val lower: T,
    actual val upper: T
) {
    init {
        require(lower <= upper) { "lower must be less than or equal to upper" }
    }

    actual fun getLower(): T = lower
    actual fun getUpper(): T = upper

    actual operator fun contains(value: T): Boolean {
        return value >= lower && value <= upper
    }

    actual operator fun contains(range: Range<T>): Boolean {
        return range.lower >= lower && range.upper <= upper
    }

    actual fun clamp(value: T): T {
        return when {
            value < lower -> lower
            value > upper -> upper
            else -> value
        }
    }

    actual fun intersect(range: Range<T>): Range<T> {
        val cmpLower = range.lower.compareTo(lower)
        val cmpUpper = range.upper.compareTo(upper)
        if (cmpLower <= 0 && cmpUpper >= 0) {
            return this
        }
        if (cmpLower >= 0 && cmpUpper <= 0) {
            return range
        }
        return create(
            if (cmpLower <= 0) lower else range.lower,
            if (cmpUpper >= 0) upper else range.upper
        )
    }

    actual fun intersect(lower: T, upper: T): Range<T> {
        val cmpLower = lower.compareTo(this.lower)
        val cmpUpper = upper.compareTo(this.upper)
        if (cmpLower <= 0 && cmpUpper >= 0) {
            return this
        }
        return create(
            if (cmpLower <= 0) this.lower else lower,
            if (cmpUpper >= 0) this.upper else upper
        )
    }

    actual fun extend(range: Range<T>): Range<T> {
        val cmpLower = range.lower.compareTo(lower)
        val cmpUpper = range.upper.compareTo(upper)
        if (cmpLower <= 0 && cmpUpper >= 0) {
            return range
        }
        if (cmpLower >= 0 && cmpUpper <= 0) {
            return this
        }
        return create(
            if (cmpLower >= 0) lower else range.lower,
            if (cmpUpper <= 0) upper else range.upper
        )
    }

    actual fun extend(lower: T, upper: T): Range<T> {
        val cmpLower = lower.compareTo(this.lower)
        val cmpUpper = upper.compareTo(this.upper)
        if (cmpLower >= 0 && cmpUpper <= 0) {
            return this
        }
        return create(
            if (cmpLower >= 0) this.lower else lower,
            if (cmpUpper <= 0) this.upper else upper
        )
    }

    actual fun extend(value: T): Range<T> {
        return extend(value, value)
    }

    actual override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other is Range<*>) {
            return lower == other.lower && upper == other.upper
        }
        return false
    }

    actual override fun toString(): String = "[$lower, $upper]"

    actual override fun hashCode(): Int {
        val h1 = lower.hashCode()
        val h2 = upper.hashCode()
        return h2 xor ((h1 shl 16) or (h1 ushr 16))
    }

    actual companion object {
        actual fun <T : Comparable<T>> create(lower: T, upper: T): Range<T> = Range(lower, upper)
    }
}
