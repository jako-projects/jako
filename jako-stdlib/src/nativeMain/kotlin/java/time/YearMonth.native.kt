package java.time

import java.io.Serializable
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalUnit
import java.time.temporal.UnsupportedTemporalTypeException

actual class YearMonth private constructor(
    actual val year: Int,
    actual val monthValue: Int
) : Temporal, TemporalAdjuster, Comparable<YearMonth>, Serializable {

    actual val month: Month get() = Month.of(monthValue)
    actual val isLeapYear: Boolean get() = isLeapYear(year)

    actual fun getYear(): Int = year
    actual fun getMonthValue(): Int = monthValue
    actual fun getMonth(): Month = month
    actual fun isLeapYear(): Boolean = isLeapYear

    actual fun isValidDay(dayOfMonth: Int): Boolean =
        dayOfMonth >= 1 && dayOfMonth <= lengthOfMonth()

    actual fun lengthOfMonth(): Int = lengthOfMonth(year, monthValue)
    actual fun lengthOfYear(): Int = lengthOfYear(year)

    actual fun withYear(year: Int): YearMonth {
        checkYear(year.toLong())
        return YearMonth(year, monthValue)
    }

    actual fun withMonth(month: Int): YearMonth = of(year, month)

    actual fun plus(amountToAdd: Long, unit: TemporalUnit): YearMonth {
        if (unit is ChronoUnit) {
            return when (unit) {
                ChronoUnit.MONTHS -> plusMonths(amountToAdd)
                ChronoUnit.YEARS -> plusYears(amountToAdd)
                ChronoUnit.DECADES -> plusYears(multiplyExact(amountToAdd, 10L))
                ChronoUnit.CENTURIES -> plusYears(multiplyExact(amountToAdd, 100L))
                ChronoUnit.MILLENNIA -> plusYears(multiplyExact(amountToAdd, 1000L))
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.addTo(this, amountToAdd)
    }

    actual fun plusYears(yearsToAdd: Long): YearMonth {
        if (yearsToAdd == 0L) return this
        return YearMonth(checkYear(year.toLong() + yearsToAdd), monthValue)
    }

    actual fun plusMonths(monthsToAdd: Long): YearMonth {
        if (monthsToAdd == 0L) return this
        val monthCount = year * 12L + (monthValue - 1)
        val calcMonths = addExact(monthCount, monthsToAdd)
        val newYear = checkYear(calcMonths.floorDiv(12L))
        val newMonth = calcMonths.mod(12L).toInt() + 1
        return YearMonth(newYear, newMonth)
    }

    actual fun minus(amountToSubtract: Long, unit: TemporalUnit): YearMonth {
        return if (amountToSubtract == Long.MIN_VALUE) {
            plus(Long.MAX_VALUE, unit).plus(1, unit)
        } else {
            plus(-amountToSubtract, unit)
        }
    }

    actual fun minusYears(yearsToSubtract: Long): YearMonth {
        return if (yearsToSubtract == Long.MIN_VALUE) {
            plusYears(Long.MAX_VALUE).plusYears(1)
        } else {
            plusYears(-yearsToSubtract)
        }
    }

    actual fun minusMonths(monthsToSubtract: Long): YearMonth {
        return if (monthsToSubtract == Long.MIN_VALUE) {
            plusMonths(Long.MAX_VALUE).plusMonths(1)
        } else {
            plusMonths(-monthsToSubtract)
        }
    }

    actual fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        val end = from(endExclusive)
        if (unit is ChronoUnit) {
            val monthsUntil = (end.year * 12L + (end.monthValue - 1)) - (year * 12L + (monthValue - 1))
            return when (unit) {
                ChronoUnit.MONTHS -> monthsUntil
                ChronoUnit.YEARS -> monthsUntil / 12L
                ChronoUnit.DECADES -> monthsUntil / 120L
                ChronoUnit.CENTURIES -> monthsUntil / 1200L
                ChronoUnit.MILLENNIA -> monthsUntil / 12000L
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.between(this, end)
    }

    actual fun atDay(dayOfMonth: Int): LocalDate = LocalDate.of(year, monthValue, dayOfMonth)
    actual fun atEndOfMonth(): LocalDate = LocalDate.of(year, monthValue, lengthOfMonth())

    actual fun isAfter(other: YearMonth): Boolean = compareTo(other) > 0
    actual fun isBefore(other: YearMonth): Boolean = compareTo(other) < 0

    actual override fun adjustInto(temporal: Temporal): Temporal {
        return when (temporal) {
            is LocalDate -> temporal.withYear(year).withMonth(monthValue)
            is LocalDateTime -> temporal.withYear(year).withMonth(monthValue)
            is YearMonth -> this
            else -> throw DateTimeException("Unable to adjust temporal with YearMonth: $temporal")
        }
    }

    actual override fun compareTo(other: YearMonth): Int {
        val cmp = year.compareTo(other.year)
        if (cmp != 0) return cmp
        return monthValue.compareTo(other.monthValue)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is YearMonth) return false
        return year == other.year && monthValue == other.monthValue
    }

    override fun hashCode(): Int = year xor (monthValue shl 27)

    override fun toString(): String {
        val absYear = kotlin.math.abs(year)
        val buf = StringBuilder(9)
        if (absYear < 1000) {
            if (year < 0) {
                buf.append(year - 10000).deleteAt(1)
            } else {
                buf.append(year + 10000).deleteAt(0)
            }
        } else {
            if (year > 9999) buf.append('+')
            buf.append(year)
        }
        return buf.append(if (monthValue < 10) "-0" else "-").append(monthValue).toString()
    }

    actual companion object {
        actual fun now(): YearMonth = now(Clock.systemDefaultZone())
        actual fun now(zone: ZoneId): YearMonth = now(Clock.system(zone))
        actual fun now(clock: Clock): YearMonth {
            val ld = LocalDate.now(clock)
            return YearMonth(ld.year, ld.monthValue)
        }

        actual fun of(year: Int, month: Month): YearMonth = of(year, month.value)

        actual fun of(year: Int, month: Int): YearMonth {
            checkYear(year.toLong())
            if (month < 1 || month > 12) {
                throw DateTimeException("Invalid value for MonthOfYear: $month")
            }
            return YearMonth(year, month)
        }

        actual fun parse(text: CharSequence): YearMonth {
            val s = text.toString()
            val dash = s.lastIndexOf('-')
            if (dash <= 0 || dash + 3 != s.length) {
                throw DateTimeParseException("Text '$s' could not be parsed as YearMonth", s, 0)
            }
            val y = s.substring(0, dash).toIntOrNull()
                ?: throw DateTimeParseException("Text '$s' could not be parsed as YearMonth", s, 0)
            val m = s.substring(dash + 1).toIntOrNull()
                ?: throw DateTimeParseException("Text '$s' could not be parsed as YearMonth", s, dash + 1)
            return try {
                of(y, m)
            } catch (ex: DateTimeException) {
                throw DateTimeParseException("Text '$s' could not be parsed as YearMonth", s, 0)
            }
        }

        actual fun from(temporal: TemporalAccessor): YearMonth {
            if (temporal is YearMonth) return temporal
            try {
                return of(
                    ChronoField.YEAR.getFrom(temporal).toInt(),
                    ChronoField.MONTH_OF_YEAR.getFrom(temporal).toInt()
                )
            } catch (ex: DateTimeException) {
                throw DateTimeException("Unable to obtain YearMonth from TemporalAccessor: $temporal", ex)
            }
        }
    }
}
