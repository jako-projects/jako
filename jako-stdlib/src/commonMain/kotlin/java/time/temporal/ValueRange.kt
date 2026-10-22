@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time.temporal

import java.io.Serializable

/**
 * The range of valid values for a date-time field.
 */
expect class ValueRange : Serializable {
    val minimum: Long
    val largestMinimum: Long
    val smallestMaximum: Long
    val maximum: Long
    val isFixed: Boolean
    val isIntValue: Boolean

    fun getMinimum(): Long
    fun getLargestMinimum(): Long
    fun getSmallestMaximum(): Long
    fun getMaximum(): Long
    fun isFixed(): Boolean
    fun isIntValue(): Boolean
    fun isValidValue(value: Long): Boolean
    fun isValidIntValue(value: Long): Boolean
    fun checkValidValue(value: Long, field: TemporalField?): Long
    fun checkValidIntValue(value: Long, field: TemporalField?): Int

    companion object {
        fun of(min: Long, max: Long): ValueRange
        fun of(min: Long, maxSmallest: Long, maxLargest: Long): ValueRange
        fun of(minSmallest: Long, minLargest: Long, maxSmallest: Long, maxLargest: Long): ValueRange
    }
}
