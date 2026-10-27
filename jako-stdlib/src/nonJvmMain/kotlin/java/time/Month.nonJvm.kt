package java.time

import java.time.temporal.ChronoField
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster

actual enum class Month : TemporalAccessor, TemporalAdjuster {
    JANUARY,
    FEBRUARY,
    MARCH,
    APRIL,
    MAY,
    JUNE,
    JULY,
    AUGUST,
    SEPTEMBER,
    OCTOBER,
    NOVEMBER,
    DECEMBER;

    actual val value: Int get() = ordinal + 1
    actual fun getValue(): Int = value

    actual fun plus(months: Long): Month {
        val amount = (months % 12).toInt()
        return values()[(ordinal + (amount + 12)) % 12]
    }

    actual fun minus(months: Long): Month = plus(-(months % 12))

    actual fun length(leapYear: Boolean): Int = when (this) {
        FEBRUARY -> if (leapYear) 29 else 28
        APRIL, JUNE, SEPTEMBER, NOVEMBER -> 30
        else -> 31
    }

    actual fun minLength(): Int = when (this) {
        FEBRUARY -> 28
        APRIL, JUNE, SEPTEMBER, NOVEMBER -> 30
        else -> 31
    }

    actual fun maxLength(): Int = when (this) {
        FEBRUARY -> 29
        APRIL, JUNE, SEPTEMBER, NOVEMBER -> 30
        else -> 31
    }

    actual fun firstDayOfYear(leapYear: Boolean): Int {
        val leap = if (leapYear) 1 else 0
        return when (this) {
            JANUARY -> 1
            FEBRUARY -> 32
            MARCH -> 60 + leap
            APRIL -> 91 + leap
            MAY -> 121 + leap
            JUNE -> 152 + leap
            JULY -> 182 + leap
            AUGUST -> 213 + leap
            SEPTEMBER -> 244 + leap
            OCTOBER -> 274 + leap
            NOVEMBER -> 305 + leap
            DECEMBER -> 335 + leap
        }
    }

    actual override fun adjustInto(temporal: Temporal): Temporal {
        return when (temporal) {
            is LocalDate -> temporal.withMonth(value)
            is LocalDateTime -> temporal.withMonth(value)
            is YearMonth -> temporal.withMonth(value)
            else -> throw DateTimeException("Unable to adjust temporal with Month: $temporal")
        }
    }

    actual companion object {
        actual fun of(month: Int): Month {
            if (month < 1 || month > 12) {
                throw DateTimeException("Invalid value for MonthOfYear: $month")
            }
            return values()[month - 1]
        }

        actual fun from(temporal: TemporalAccessor): Month {
            if (temporal is Month) return temporal
            try {
                return of(ChronoField.MONTH_OF_YEAR.getFrom(temporal).toInt())
            } catch (ex: DateTimeException) {
                throw DateTimeException("Unable to obtain Month from TemporalAccessor: $temporal", ex)
            }
        }
    }
}
