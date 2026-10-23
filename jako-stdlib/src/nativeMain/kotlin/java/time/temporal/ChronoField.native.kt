package java.time.temporal

import java.time.DayOfWeek
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
