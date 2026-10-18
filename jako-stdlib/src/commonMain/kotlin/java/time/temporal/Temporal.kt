@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time.temporal

import java.io.Serializable
import java.time.DateTimeException
import java.time.DayOfWeek
import java.time.Duration

/**
 * Marker for types that can be formatted/parsed via [java.time.format.DateTimeFormatter].
 */
expect interface TemporalAccessor

/**
 * Framework-level interface defining read-write access to a temporal object.
 */
expect interface Temporal : TemporalAccessor

/**
 * Framework-level interface defining an amount of time, such as "6 hours" or "2 years and 3 months".
 */
expect interface TemporalAmount

/**
 * Strategy for adjusting a temporal object.
 */
expect fun interface TemporalAdjuster {
    fun adjustInto(temporal: Temporal): Temporal
}

/**
 * Common and useful TemporalAdjuster implementations.
 */
expect class TemporalAdjusters {
    companion object {
        fun firstDayOfMonth(): TemporalAdjuster
        fun lastDayOfMonth(): TemporalAdjuster
        fun firstDayOfNextMonth(): TemporalAdjuster
        fun firstDayOfYear(): TemporalAdjuster
        fun lastDayOfYear(): TemporalAdjuster
        fun firstDayOfNextYear(): TemporalAdjuster
        fun firstInMonth(dayOfWeek: DayOfWeek): TemporalAdjuster
        fun lastInMonth(dayOfWeek: DayOfWeek): TemporalAdjuster
        fun dayOfWeekInMonth(ordinal: Int, dayOfWeek: DayOfWeek): TemporalAdjuster
        fun next(dayOfWeek: DayOfWeek): TemporalAdjuster
        fun nextOrSame(dayOfWeek: DayOfWeek): TemporalAdjuster
        fun previous(dayOfWeek: DayOfWeek): TemporalAdjuster
        fun previousOrSame(dayOfWeek: DayOfWeek): TemporalAdjuster
    }
}

/**
 * Indicates that a ChronoField or ChronoUnit is not supported for a Temporal class.
 */
expect open class UnsupportedTemporalTypeException : DateTimeException {
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
}

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

/**
 * A field of date-time, such as month-of-year or hour-of-minute.
 */
expect interface TemporalField {
    val isDateBased: Boolean
    val isTimeBased: Boolean
    fun isDateBased(): Boolean
    fun isTimeBased(): Boolean
    fun range(): ValueRange
    fun isSupportedBy(temporal: TemporalAccessor): Boolean
    fun rangeRefinedBy(temporal: TemporalAccessor): ValueRange
    fun getFrom(temporal: TemporalAccessor): Long
}

/**
 * A standard set of fields.
 */
expect enum class ChronoField : TemporalField {
    NANO_OF_SECOND,
    NANO_OF_DAY,
    MICRO_OF_SECOND,
    MICRO_OF_DAY,
    MILLI_OF_SECOND,
    MILLI_OF_DAY,
    SECOND_OF_MINUTE,
    SECOND_OF_DAY,
    MINUTE_OF_HOUR,
    MINUTE_OF_DAY,
    HOUR_OF_AMPM,
    CLOCK_HOUR_OF_AMPM,
    HOUR_OF_DAY,
    CLOCK_HOUR_OF_DAY,
    AMPM_OF_DAY,
    DAY_OF_WEEK,
    ALIGNED_DAY_OF_WEEK_IN_MONTH,
    ALIGNED_DAY_OF_WEEK_IN_YEAR,
    DAY_OF_MONTH,
    DAY_OF_YEAR,
    EPOCH_DAY,
    ALIGNED_WEEK_OF_MONTH,
    ALIGNED_WEEK_OF_YEAR,
    MONTH_OF_YEAR,
    PROLEPTIC_MONTH,
    YEAR_OF_ERA,
    YEAR,
    ERA,
    INSTANT_SECONDS,
    OFFSET_SECONDS;

    override val isDateBased: Boolean
    override val isTimeBased: Boolean
    override fun isDateBased(): Boolean
    override fun isTimeBased(): Boolean
    override fun range(): ValueRange
    override fun isSupportedBy(temporal: TemporalAccessor): Boolean
    override fun rangeRefinedBy(temporal: TemporalAccessor): ValueRange
    override fun getFrom(temporal: TemporalAccessor): Long
    fun checkValidValue(value: Long): Long
    fun checkValidIntValue(value: Long): Int
}

/**
 * A unit of date-time, such as Days or Hours.
 */
expect interface TemporalUnit {
    val duration: Duration
    val isDurationEstimated: Boolean
    val isDateBased: Boolean
    val isTimeBased: Boolean
    fun getDuration(): Duration
    fun isDurationEstimated(): Boolean
    fun isDateBased(): Boolean
    fun isTimeBased(): Boolean
    fun isSupportedBy(temporal: Temporal): Boolean
    fun <R : Temporal> addTo(temporal: R, amount: Long): R
    fun between(temporal1Inclusive: Temporal, temporal2Exclusive: Temporal): Long
}

/**
 * A standard set of date periods units.
 */
expect enum class ChronoUnit : TemporalUnit {
    NANOS,
    MICROS,
    MILLIS,
    SECONDS,
    MINUTES,
    HOURS,
    HALF_DAYS,
    DAYS,
    WEEKS,
    MONTHS,
    YEARS,
    DECADES,
    CENTURIES,
    MILLENNIA,
    ERAS,
    FOREVER;

    override val duration: Duration
    override val isDurationEstimated: Boolean
    override val isDateBased: Boolean
    override val isTimeBased: Boolean
    override fun getDuration(): Duration
    override fun isDurationEstimated(): Boolean
    override fun isDateBased(): Boolean
    override fun isTimeBased(): Boolean
    override fun isSupportedBy(temporal: Temporal): Boolean
    override fun <R : Temporal> addTo(temporal: R, amount: Long): R
    override fun between(temporal1Inclusive: Temporal, temporal2Exclusive: Temporal): Long
}
