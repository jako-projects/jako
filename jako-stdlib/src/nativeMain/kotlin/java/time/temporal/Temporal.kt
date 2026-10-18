package java.time.temporal

import java.io.Serializable
import java.time.DateTimeException
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.MonthDay
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Year
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.addExact
import java.time.isLeapYear
import java.time.lengthOfMonth

actual interface TemporalAccessor

actual interface Temporal : TemporalAccessor

actual interface TemporalAmount

actual fun interface TemporalAdjuster {
    actual fun adjustInto(temporal: Temporal): Temporal
}

actual class TemporalAdjusters private constructor() {
    actual companion object {
        actual fun firstDayOfMonth(): TemporalAdjuster = TemporalAdjuster { temporal ->
            when (temporal) {
                is LocalDate -> temporal.withDayOfMonth(1)
                is LocalDateTime -> temporal.withDayOfMonth(1)
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for firstDayOfMonth: $temporal")
            }
        }

        actual fun lastDayOfMonth(): TemporalAdjuster = TemporalAdjuster { temporal ->
            when (temporal) {
                is LocalDate -> temporal.withDayOfMonth(temporal.lengthOfMonth())
                is LocalDateTime -> temporal.withDayOfMonth(temporal.toLocalDate().lengthOfMonth())
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for lastDayOfMonth: $temporal")
            }
        }

        actual fun firstDayOfNextMonth(): TemporalAdjuster = TemporalAdjuster { temporal ->
            when (temporal) {
                is LocalDate -> temporal.withDayOfMonth(1).plusMonths(1)
                is LocalDateTime -> temporal.withDayOfMonth(1).plusMonths(1)
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for firstDayOfNextMonth: $temporal")
            }
        }

        actual fun firstDayOfYear(): TemporalAdjuster = TemporalAdjuster { temporal ->
            when (temporal) {
                is LocalDate -> temporal.withDayOfYear(1)
                is LocalDateTime -> temporal.withDayOfYear(1)
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for firstDayOfYear: $temporal")
            }
        }

        actual fun lastDayOfYear(): TemporalAdjuster = TemporalAdjuster { temporal ->
            when (temporal) {
                is LocalDate -> temporal.withDayOfYear(temporal.lengthOfYear())
                is LocalDateTime -> temporal.withDayOfYear(temporal.toLocalDate().lengthOfYear())
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for lastDayOfYear: $temporal")
            }
        }

        actual fun firstDayOfNextYear(): TemporalAdjuster = TemporalAdjuster { temporal ->
            when (temporal) {
                is LocalDate -> temporal.withDayOfYear(1).plusYears(1)
                is LocalDateTime -> temporal.withDayOfYear(1).plusYears(1)
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for firstDayOfNextYear: $temporal")
            }
        }

        actual fun firstInMonth(dayOfWeek: DayOfWeek): TemporalAdjuster =
            dayOfWeekInMonth(1, dayOfWeek)

        actual fun lastInMonth(dayOfWeek: DayOfWeek): TemporalAdjuster =
            dayOfWeekInMonth(-1, dayOfWeek)

        actual fun dayOfWeekInMonth(ordinal: Int, dayOfWeek: DayOfWeek): TemporalAdjuster {
            if (ordinal == 0) {
                return TemporalAdjuster { temporal ->
                    val firstOfCurrent = firstDayOfMonth().adjustInto(temporal)
                    previous(dayOfWeek).adjustInto(firstOfCurrent)
                }
            }
            if (ordinal > 0) {
                return TemporalAdjuster { temporal ->
                    val firstOfCurrent = firstDayOfMonth().adjustInto(temporal)
                    val firstDow = nextOrSame(dayOfWeek).adjustInto(firstOfCurrent)
                    val daysToAdd = (ordinal.toLong() - 1L) * 7L
                    when (firstDow) {
                        is LocalDate -> firstDow.plusDays(daysToAdd)
                        is LocalDateTime -> firstDow.plusDays(daysToAdd)
                        else -> ChronoUnit.DAYS.addTo(firstDow, daysToAdd)
                    }
                }
            }
            return TemporalAdjuster { temporal ->
                val lastOfCurrent = lastDayOfMonth().adjustInto(temporal)
                val lastDow = previousOrSame(dayOfWeek).adjustInto(lastOfCurrent)
                val daysToSubtract = (-ordinal.toLong() - 1L) * 7L
                when (lastDow) {
                    is LocalDate -> lastDow.minusDays(daysToSubtract)
                    is LocalDateTime -> lastDow.minusDays(daysToSubtract)
                    else -> ChronoUnit.DAYS.addTo(lastDow, -daysToSubtract)
                }
            }
        }

        actual fun next(dayOfWeek: DayOfWeek): TemporalAdjuster = TemporalAdjuster { temporal ->
            val targetDow = dayOfWeek.value
            when (temporal) {
                is LocalDate -> {
                    val calDow = temporal.dayOfWeek.value
                    val daysDiff = calDow - targetDow
                    temporal.plusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                }
                is LocalDateTime -> {
                    val calDow = temporal.dayOfWeek.value
                    val daysDiff = calDow - targetDow
                    temporal.plusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                }
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for next: $temporal")
            }
        }

        actual fun nextOrSame(dayOfWeek: DayOfWeek): TemporalAdjuster = TemporalAdjuster { temporal ->
            val targetDow = dayOfWeek.value
            when (temporal) {
                is LocalDate -> {
                    val calDow = temporal.dayOfWeek.value
                    if (calDow == targetDow) temporal else {
                        val daysDiff = calDow - targetDow
                        temporal.plusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                    }
                }
                is LocalDateTime -> {
                    val calDow = temporal.dayOfWeek.value
                    if (calDow == targetDow) temporal else {
                        val daysDiff = calDow - targetDow
                        temporal.plusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                    }
                }
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for nextOrSame: $temporal")
            }
        }

        actual fun previous(dayOfWeek: DayOfWeek): TemporalAdjuster = TemporalAdjuster { temporal ->
            val targetDow = dayOfWeek.value
            when (temporal) {
                is LocalDate -> {
                    val calDow = temporal.dayOfWeek.value
                    val daysDiff = targetDow - calDow
                    temporal.minusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                }
                is LocalDateTime -> {
                    val calDow = temporal.dayOfWeek.value
                    val daysDiff = targetDow - calDow
                    temporal.minusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                }
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for previous: $temporal")
            }
        }

        actual fun previousOrSame(dayOfWeek: DayOfWeek): TemporalAdjuster = TemporalAdjuster { temporal ->
            val targetDow = dayOfWeek.value
            when (temporal) {
                is LocalDate -> {
                    val calDow = temporal.dayOfWeek.value
                    if (calDow == targetDow) temporal else {
                        val daysDiff = targetDow - calDow
                        temporal.minusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                    }
                }
                is LocalDateTime -> {
                    val calDow = temporal.dayOfWeek.value
                    if (calDow == targetDow) temporal else {
                        val daysDiff = targetDow - calDow
                        temporal.minusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                    }
                }
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for previousOrSame: $temporal")
            }
        }
    }
}

actual open class UnsupportedTemporalTypeException : DateTimeException {
    actual constructor(message: String?) : super(message)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
}

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

actual interface TemporalField {
    actual val isDateBased: Boolean
    actual val isTimeBased: Boolean
    actual fun isDateBased(): Boolean
    actual fun isTimeBased(): Boolean
    actual fun range(): ValueRange
    actual fun isSupportedBy(temporal: TemporalAccessor): Boolean
    actual fun rangeRefinedBy(temporal: TemporalAccessor): ValueRange
    actual fun getFrom(temporal: TemporalAccessor): Long
}

actual enum class ChronoField(
    private val fieldRange: ValueRange,
    private val dateBased: Boolean,
    private val timeBased: Boolean
) : TemporalField {
    NANO_OF_SECOND(ValueRange.of(0, 999_999_999), false, true),
    NANO_OF_DAY(ValueRange.of(0, 86400L * 1_000_000_000L - 1), false, true),
    MICRO_OF_SECOND(ValueRange.of(0, 999_999), false, true),
    MICRO_OF_DAY(ValueRange.of(0, 86400L * 1_000_000L - 1), false, true),
    MILLI_OF_SECOND(ValueRange.of(0, 999), false, true),
    MILLI_OF_DAY(ValueRange.of(0, 86400L * 1_000L - 1), false, true),
    SECOND_OF_MINUTE(ValueRange.of(0, 59), false, true),
    SECOND_OF_DAY(ValueRange.of(0, 86400L - 1), false, true),
    MINUTE_OF_HOUR(ValueRange.of(0, 59), false, true),
    MINUTE_OF_DAY(ValueRange.of(0, (24 * 60) - 1), false, true),
    HOUR_OF_AMPM(ValueRange.of(0, 11), false, true),
    CLOCK_HOUR_OF_AMPM(ValueRange.of(1, 12), false, true),
    HOUR_OF_DAY(ValueRange.of(0, 23), false, true),
    CLOCK_HOUR_OF_DAY(ValueRange.of(1, 24), false, true),
    AMPM_OF_DAY(ValueRange.of(0, 1), false, true),
    DAY_OF_WEEK(ValueRange.of(1, 7), true, false),
    ALIGNED_DAY_OF_WEEK_IN_MONTH(ValueRange.of(1, 7), true, false),
    ALIGNED_DAY_OF_WEEK_IN_YEAR(ValueRange.of(1, 7), true, false),
    DAY_OF_MONTH(ValueRange.of(1, 28, 31), true, false),
    DAY_OF_YEAR(ValueRange.of(1, 365, 366), true, false),
    EPOCH_DAY(ValueRange.of(-365243219162L, 365241780471L), true, false),
    ALIGNED_WEEK_OF_MONTH(ValueRange.of(1, 4, 5), true, false),
    ALIGNED_WEEK_OF_YEAR(ValueRange.of(1, 53), true, false),
    MONTH_OF_YEAR(ValueRange.of(1, 12), true, false),
    PROLEPTIC_MONTH(ValueRange.of(-999_999_999L * 12L, 999_999_999L * 12L + 11L), true, false),
    YEAR_OF_ERA(ValueRange.of(1, 999_999_999L, 1_000_000_000L), true, false),
    YEAR(ValueRange.of(-999_999_999L, 999_999_999L), true, false),
    ERA(ValueRange.of(0, 1), true, false),
    INSTANT_SECONDS(ValueRange.of(Long.MIN_VALUE, Long.MAX_VALUE), false, false),
    OFFSET_SECONDS(ValueRange.of(-18 * 3600, 18 * 3600), false, false);

    actual override val isDateBased: Boolean get() = dateBased
    actual override val isTimeBased: Boolean get() = timeBased
    actual override fun isDateBased(): Boolean = dateBased
    actual override fun isTimeBased(): Boolean = timeBased
    actual override fun range(): ValueRange = fieldRange

    actual fun checkValidValue(value: Long): Long = fieldRange.checkValidValue(value, this)
    actual fun checkValidIntValue(value: Long): Int = fieldRange.checkValidIntValue(value, this)

    actual override fun isSupportedBy(temporal: TemporalAccessor): Boolean {
        return when (temporal) {
            is LocalDate -> dateBased
            is LocalTime -> timeBased
            is LocalDateTime -> dateBased || timeBased
            is OffsetDateTime, is ZonedDateTime -> true
            is OffsetTime -> timeBased || this == OFFSET_SECONDS
            is Instant -> this == NANO_OF_SECOND || this == MICRO_OF_SECOND || this == MILLI_OF_SECOND || this == INSTANT_SECONDS
            is Year -> this == YEAR || this == YEAR_OF_ERA || this == ERA
            is YearMonth -> this == YEAR || this == YEAR_OF_ERA || this == MONTH_OF_YEAR || this == PROLEPTIC_MONTH || this == ERA
            is MonthDay -> this == MONTH_OF_YEAR || this == DAY_OF_MONTH
            is Month -> this == MONTH_OF_YEAR
            is DayOfWeek -> this == DAY_OF_WEEK
            is ZoneOffset -> this == OFFSET_SECONDS
            else -> false
        }
    }

    actual override fun rangeRefinedBy(temporal: TemporalAccessor): ValueRange {
        if (!isSupportedBy(temporal)) {
            throw UnsupportedTemporalTypeException("Unsupported field: $this")
        }
        val date = when (temporal) {
            is LocalDate -> temporal
            is LocalDateTime -> temporal.toLocalDate()
            is OffsetDateTime -> temporal.toLocalDate()
            is ZonedDateTime -> temporal.toLocalDate()
            else -> null
        }
        if (date != null) {
            return when (this) {
                DAY_OF_MONTH -> ValueRange.of(1, date.lengthOfMonth().toLong())
                DAY_OF_YEAR -> ValueRange.of(1, date.lengthOfYear().toLong())
                ALIGNED_WEEK_OF_MONTH -> ValueRange.of(1, if (date.month == Month.FEBRUARY && !date.isLeapYear()) 4L else 5L)
                YEAR_OF_ERA -> if (date.year <= 0) ValueRange.of(1, 1_000_000_000L) else ValueRange.of(1, 999_999_999L)
                else -> fieldRange
            }
        }
        if (temporal is YearMonth && this == DAY_OF_MONTH) {
            return ValueRange.of(1, temporal.lengthOfMonth().toLong())
        }
        return fieldRange
    }

    actual override fun getFrom(temporal: TemporalAccessor): Long {
        if (!isSupportedBy(temporal)) {
            throw UnsupportedTemporalTypeException("Unsupported field: $this")
        }
        return when (temporal) {
            is LocalDate -> getFromDate(temporal)
            is LocalTime -> getFromTime(temporal)
            is LocalDateTime -> if (dateBased) getFromDate(temporal.toLocalDate()) else getFromTime(temporal.toLocalTime())
            is OffsetDateTime -> when {
                this == INSTANT_SECONDS -> temporal.toEpochSecond()
                this == OFFSET_SECONDS -> temporal.offset.totalSeconds.toLong()
                dateBased -> getFromDate(temporal.toLocalDate())
                else -> getFromTime(temporal.toLocalTime())
            }
            is ZonedDateTime -> when {
                this == INSTANT_SECONDS -> temporal.toEpochSecond()
                this == OFFSET_SECONDS -> temporal.offset.totalSeconds.toLong()
                dateBased -> getFromDate(temporal.toLocalDate())
                else -> getFromTime(temporal.toLocalTime())
            }
            is OffsetTime -> if (this == OFFSET_SECONDS) temporal.offset.totalSeconds.toLong() else getFromTime(temporal.toLocalTime())
            is Instant -> when (this) {
                NANO_OF_SECOND -> temporal.nano.toLong()
                MICRO_OF_SECOND -> (temporal.nano / 1_000).toLong()
                MILLI_OF_SECOND -> (temporal.nano / 1_000_000).toLong()
                INSTANT_SECONDS -> temporal.epochSecond
                else -> throw UnsupportedTemporalTypeException("Unsupported field: $this")
            }
            is Year -> when (this) {
                YEAR -> temporal.value.toLong()
                YEAR_OF_ERA -> if (temporal.value < 1) (1 - temporal.value).toLong() else temporal.value.toLong()
                ERA -> if (temporal.value < 1) 0L else 1L
                else -> throw UnsupportedTemporalTypeException("Unsupported field: $this")
            }
            is YearMonth -> when (this) {
                MONTH_OF_YEAR -> temporal.monthValue.toLong()
                PROLEPTIC_MONTH -> temporal.year * 12L + temporal.monthValue - 1L
                YEAR_OF_ERA -> if (temporal.year < 1) (1 - temporal.year).toLong() else temporal.year.toLong()
                YEAR -> temporal.year.toLong()
                ERA -> if (temporal.year < 1) 0L else 1L
                else -> throw UnsupportedTemporalTypeException("Unsupported field: $this")
            }
            is MonthDay -> when (this) {
                MONTH_OF_YEAR -> temporal.monthValue.toLong()
                DAY_OF_MONTH -> temporal.dayOfMonth.toLong()
                else -> throw UnsupportedTemporalTypeException("Unsupported field: $this")
            }
            is Month -> temporal.value.toLong()
            is DayOfWeek -> temporal.value.toLong()
            is ZoneOffset -> temporal.totalSeconds.toLong()
            else -> throw UnsupportedTemporalTypeException("Unsupported field: $this")
        }
    }

    private fun getFromDate(date: LocalDate): Long = when (this) {
        DAY_OF_WEEK -> date.dayOfWeek.value.toLong()
        ALIGNED_DAY_OF_WEEK_IN_MONTH -> ((date.dayOfMonth - 1) % 7 + 1).toLong()
        ALIGNED_DAY_OF_WEEK_IN_YEAR -> ((date.dayOfYear - 1) % 7 + 1).toLong()
        DAY_OF_MONTH -> date.dayOfMonth.toLong()
        DAY_OF_YEAR -> date.dayOfYear.toLong()
        EPOCH_DAY -> date.toEpochDay()
        ALIGNED_WEEK_OF_MONTH -> ((date.dayOfMonth - 1) / 7 + 1).toLong()
        ALIGNED_WEEK_OF_YEAR -> ((date.dayOfYear - 1) / 7 + 1).toLong()
        MONTH_OF_YEAR -> date.monthValue.toLong()
        PROLEPTIC_MONTH -> date.year * 12L + date.monthValue - 1L
        YEAR_OF_ERA -> if (date.year >= 1) date.year.toLong() else (1 - date.year).toLong()
        YEAR -> date.year.toLong()
        ERA -> if (date.year >= 1) 1L else 0L
        else -> throw UnsupportedTemporalTypeException("Unsupported field: $this")
    }

    private fun getFromTime(time: LocalTime): Long = when (this) {
        NANO_OF_SECOND -> time.nano.toLong()
        NANO_OF_DAY -> time.toNanoOfDay()
        MICRO_OF_SECOND -> (time.nano / 1_000).toLong()
        MICRO_OF_DAY -> time.toNanoOfDay() / 1_000L
        MILLI_OF_SECOND -> (time.nano / 1_000_000).toLong()
        MILLI_OF_DAY -> time.toNanoOfDay() / 1_000_000L
        SECOND_OF_MINUTE -> time.second.toLong()
        SECOND_OF_DAY -> time.toSecondOfDay().toLong()
        MINUTE_OF_HOUR -> time.minute.toLong()
        MINUTE_OF_DAY -> (time.hour * 60 + time.minute).toLong()
        HOUR_OF_AMPM -> (time.hour % 12).toLong()
        CLOCK_HOUR_OF_AMPM -> {
            val ham = time.hour % 12
            if (ham == 0) 12L else ham.toLong()
        }
        HOUR_OF_DAY -> time.hour.toLong()
        CLOCK_HOUR_OF_DAY -> if (time.hour == 0) 24L else time.hour.toLong()
        AMPM_OF_DAY -> (time.hour / 12).toLong()
        else -> throw UnsupportedTemporalTypeException("Unsupported field: $this")
    }
}

actual interface TemporalUnit {
    actual val duration: Duration
    actual val isDurationEstimated: Boolean
    actual val isDateBased: Boolean
    actual val isTimeBased: Boolean
    actual fun getDuration(): Duration
    actual fun isDurationEstimated(): Boolean
    actual fun isDateBased(): Boolean
    actual fun isTimeBased(): Boolean
    actual fun isSupportedBy(temporal: Temporal): Boolean
    actual fun <R : Temporal> addTo(temporal: R, amount: Long): R
    actual fun between(temporal1Inclusive: Temporal, temporal2Exclusive: Temporal): Long
}

actual enum class ChronoUnit(
    private val unitDuration: Duration,
    private val estimated: Boolean,
    private val dateBased: Boolean,
    private val timeBased: Boolean
) : TemporalUnit {
    NANOS(Duration.ofNanos(1), false, false, true),
    MICROS(Duration.ofNanos(1_000), false, false, true),
    MILLIS(Duration.ofNanos(1_000_000), false, false, true),
    SECONDS(Duration.ofSeconds(1), false, false, true),
    MINUTES(Duration.ofSeconds(60), false, false, true),
    HOURS(Duration.ofSeconds(3600), false, false, true),
    HALF_DAYS(Duration.ofSeconds(43200), false, false, true),
    DAYS(Duration.ofSeconds(86400), true, true, false),
    WEEKS(Duration.ofSeconds(7 * 86400L), true, true, false),
    MONTHS(Duration.ofSeconds(31556952L / 12), true, true, false),
    YEARS(Duration.ofSeconds(31556952L), true, true, false),
    DECADES(Duration.ofSeconds(31556952L * 10L), true, true, false),
    CENTURIES(Duration.ofSeconds(31556952L * 100L), true, true, false),
    MILLENNIA(Duration.ofSeconds(31556952L * 1000L), true, true, false),
    ERAS(Duration.ofSeconds(31556952L * 1_000_000_000L), true, true, false),
    FOREVER(Duration.ofSeconds(Long.MAX_VALUE, 999_999_999L), true, false, false);

    actual override val duration: Duration get() = unitDuration
    actual override val isDurationEstimated: Boolean get() = estimated
    actual override val isDateBased: Boolean get() = dateBased
    actual override val isTimeBased: Boolean get() = timeBased
    actual override fun getDuration(): Duration = unitDuration
    actual override fun isDurationEstimated(): Boolean = estimated
    actual override fun isDateBased(): Boolean = dateBased
    actual override fun isTimeBased(): Boolean = timeBased

    actual override fun isSupportedBy(temporal: Temporal): Boolean {
        if (this == FOREVER) return false
        return when (temporal) {
            is LocalDate -> dateBased
            is LocalTime, is OffsetTime -> timeBased
            is LocalDateTime, is OffsetDateTime, is ZonedDateTime -> dateBased || timeBased
            is Instant -> timeBased || this == DAYS
            is Year -> this == YEARS || this == DECADES || this == CENTURIES || this == MILLENNIA || this == ERAS
            is YearMonth -> this == MONTHS || this == YEARS || this == DECADES || this == CENTURIES || this == MILLENNIA || this == ERAS
            else -> false
        }
    }

    @Suppress("UNCHECKED_CAST")
    actual override fun <R : Temporal> addTo(temporal: R, amount: Long): R {
        return when (temporal) {
            is LocalDate -> temporal.plus(amount, this) as R
            is LocalTime -> temporal.plus(amount, this) as R
            is LocalDateTime -> temporal.plus(amount, this) as R
            is Instant -> temporal.plus(amount, this) as R
            is OffsetDateTime -> OffsetDateTime.of(temporal.toLocalDateTime().plus(amount, this), temporal.offset) as R
            is ZonedDateTime -> ZonedDateTime.of(temporal.toLocalDateTime().plus(amount, this), temporal.zone) as R
            is Year -> when (this) {
                YEARS -> temporal.plusYears(amount) as R
                DECADES -> temporal.plusYears(amount * 10L) as R
                CENTURIES -> temporal.plusYears(amount * 100L) as R
                MILLENNIA -> temporal.plusYears(amount * 1000L) as R
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $this")
            }
            is YearMonth -> when (this) {
                MONTHS -> temporal.plusMonths(amount) as R
                YEARS -> temporal.plusYears(amount) as R
                DECADES -> temporal.plusYears(amount * 10L) as R
                CENTURIES -> temporal.plusYears(amount * 100L) as R
                MILLENNIA -> temporal.plusYears(amount * 1000L) as R
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $this")
            }
            else -> throw UnsupportedTemporalTypeException("Unsupported temporal for addTo: $temporal")
        }
    }

    actual override fun between(temporal1Inclusive: Temporal, temporal2Exclusive: Temporal): Long {
        return when {
            temporal1Inclusive is LocalDate && temporal2Exclusive is LocalDate ->
                temporal1Inclusive.until(temporal2Exclusive, this)
            temporal1Inclusive is LocalTime && temporal2Exclusive is LocalTime ->
                temporal1Inclusive.until(temporal2Exclusive, this)
            temporal1Inclusive is LocalDateTime && temporal2Exclusive is LocalDateTime ->
                temporal1Inclusive.until(temporal2Exclusive, this)
            temporal1Inclusive is Instant && temporal2Exclusive is Instant ->
                temporal1Inclusive.until(temporal2Exclusive, this)
            temporal1Inclusive is OffsetDateTime && temporal2Exclusive is OffsetDateTime ->
                temporal1Inclusive.toInstant().until(temporal2Exclusive.toInstant(), this)
            temporal1Inclusive is ZonedDateTime && temporal2Exclusive is ZonedDateTime ->
                temporal1Inclusive.toInstant().until(temporal2Exclusive.toInstant(), this)
            else -> throw UnsupportedTemporalTypeException("Unsupported temporals for between: $temporal1Inclusive, $temporal2Exclusive")
        }
    }
}
