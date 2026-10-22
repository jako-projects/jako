package java.time.temporal

import java.io.Serializable
import java.time.DateTimeException
import java.time.addExact
import java.time.isLeapYear
import java.time.lengthOfMonth

actual class ValueRange private constructor(
    actual val minimum: Long,
    actual val largestMinimum: Long,
    actual val smallestMaximum: Long,
    actual val maximum: Long
) : Serializable {
    actual val isFixed: Boolean
        get() = minimum == largestMinimum && smallestMaximum == maximum

    actual val isIntValue: Boolean
        get() = minimum >= Int.MIN_VALUE && maximum <= Int.MAX_VALUE

    actual fun getMinimum(): Long = minimum
    actual fun getLargestMinimum(): Long = largestMinimum
    actual fun getSmallestMaximum(): Long = smallestMaximum
    actual fun getMaximum(): Long = maximum
    actual fun isFixed(): Boolean = isFixed
    actual fun isIntValue(): Boolean = isIntValue

    actual fun isValidValue(value: Long): Boolean = value in minimum..maximum

    actual fun isValidIntValue(value: Long): Boolean = isIntValue && isValidValue(value)

    actual fun checkValidValue(value: Long, field: TemporalField?): Long {
        if (!isValidValue(value)) {
            throw DateTimeException("Invalid value for ${field ?: "field"} (valid values $this): $value")
        }
        return value
    }

    actual fun checkValidIntValue(value: Long, field: TemporalField?): Int {
        if (!isValidIntValue(value)) {
            throw DateTimeException("Invalid int value for ${field ?: "field"} (valid values $this): $value")
        }
        return value.toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValueRange) return false
        return minimum == other.minimum &&
            largestMinimum == other.largestMinimum &&
            smallestMaximum == other.smallestMaximum &&
            maximum == other.maximum
    }

    override fun hashCode(): Int {
        val hash = minimum + (largestMinimum shl 16) + (largestMinimum shr 48) +
            (smallestMaximum shl 32) + (smallestMaximum shr 32) + (maximum shl 48) + (maximum shr 16)
        return (hash xor (hash ushr 32)).toInt()
    }

    override fun toString(): String {
        val buf = StringBuilder()
        buf.append(minimum)
        if (minimum != largestMinimum) {
            buf.append('/').append(largestMinimum)
        }
        buf.append(" - ").append(smallestMaximum)
        if (smallestMaximum != maximum) {
            buf.append('/').append(maximum)
        }
        return buf.toString()
    }

    actual companion object {
        actual fun of(min: Long, max: Long): ValueRange {
            if (min > max) throw IllegalArgumentException("Minimum value must be less than maximum value")
            return ValueRange(min, min, max, max)
        }

        actual fun of(min: Long, maxSmallest: Long, maxLargest: Long): ValueRange {
            if (min > maxSmallest) throw IllegalArgumentException("Minimum value must be less than smallest maximum value")
            return of(min, min, maxSmallest, maxLargest)
        }

        actual fun of(minSmallest: Long, minLargest: Long, maxSmallest: Long, maxLargest: Long): ValueRange {
            if (minSmallest > minLargest) throw IllegalArgumentException("Smallest minimum value must be less than largest minimum value")
            if (maxSmallest > maxLargest) throw IllegalArgumentException("Smallest maximum value must be less than largest maximum value")
            if (minLargest > maxLargest) throw IllegalArgumentException("Minimum value must be less than maximum value")
            return ValueRange(minSmallest, minLargest, maxSmallest, maxLargest)
        }
    }
}
