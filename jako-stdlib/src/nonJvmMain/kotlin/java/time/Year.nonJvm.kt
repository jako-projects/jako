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

actual class Year private constructor(
    actual val value: Int
) : Temporal, TemporalAdjuster, Comparable<Year>, Serializable {

    actual val isLeap: Boolean get() = isLeap(value.toLong())

    actual fun getValue(): Int = value
    actual fun isLeap(): Boolean = isLeap
    actual fun length(): Int = if (isLeap) 366 else 365

    actual fun plus(amountToAdd: Long, unit: TemporalUnit): Year {
        if (unit is ChronoUnit) {
            return when (unit) {
                ChronoUnit.YEARS -> plusYears(amountToAdd)
                ChronoUnit.DECADES -> plusYears(multiplyExact(amountToAdd, 10L))
                ChronoUnit.CENTURIES -> plusYears(multiplyExact(amountToAdd, 100L))
                ChronoUnit.MILLENNIA -> plusYears(multiplyExact(amountToAdd, 1000L))
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.addTo(this, amountToAdd)
    }

    actual fun plusYears(yearsToAdd: Long): Year {
        if (yearsToAdd == 0L) return this
        return of(checkYear(value.toLong() + yearsToAdd))
    }

    actual fun minus(amountToSubtract: Long, unit: TemporalUnit): Year {
        return if (amountToSubtract == Long.MIN_VALUE) {
            plus(Long.MAX_VALUE, unit).plus(1, unit)
        } else {
            plus(-amountToSubtract, unit)
        }
    }

    actual fun minusYears(yearsToSubtract: Long): Year {
        return if (yearsToSubtract == Long.MIN_VALUE) {
            plusYears(Long.MAX_VALUE).plusYears(1)
        } else {
            plusYears(-yearsToSubtract)
        }
    }

    actual fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        val end = from(endExclusive)
        if (unit is ChronoUnit) {
            val yearsUntil = end.value.toLong() - value.toLong()
            return when (unit) {
                ChronoUnit.YEARS -> yearsUntil
                ChronoUnit.DECADES -> yearsUntil / 10L
                ChronoUnit.CENTURIES -> yearsUntil / 100L
                ChronoUnit.MILLENNIA -> yearsUntil / 1000L
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.between(this, end)
    }

    actual fun atDay(dayOfYear: Int): LocalDate = LocalDate.ofYearDay(value, dayOfYear)
    actual fun atMonth(month: Month): YearMonth = YearMonth.of(value, month)
    actual fun atMonth(month: Int): YearMonth = YearMonth.of(value, month)
    actual fun atMonthDay(monthDay: MonthDay): LocalDate = monthDay.atYear(value)

    actual fun isAfter(other: Year): Boolean = value > other.value
    actual fun isBefore(other: Year): Boolean = value < other.value

    actual override fun adjustInto(temporal: Temporal): Temporal {
        return when (temporal) {
            is LocalDate -> temporal.withYear(value)
            is LocalDateTime -> temporal.withYear(value)
            is YearMonth -> temporal.withYear(value)
            is Year -> this
            else -> throw DateTimeException("Unable to adjust temporal with Year: $temporal")
        }
    }

    actual override fun compareTo(other: Year): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Year) return false
        return value == other.value
    }

    override fun hashCode(): Int = value

    override fun toString(): String = value.toString()

    actual companion object {
        actual val MIN_VALUE: Int = YEAR_MIN
        actual val MAX_VALUE: Int = YEAR_MAX

        actual fun now(): Year = now(Clock.systemDefaultZone())
        actual fun now(zone: ZoneId): Year = now(Clock.system(zone))
        actual fun now(clock: Clock): Year = of(LocalDate.now(clock).year)

        actual fun of(isoYear: Int): Year {
            checkYear(isoYear.toLong())
            return Year(isoYear)
        }

        actual fun isLeap(year: Long): Boolean {
            return (year and 3L) == 0L && ((year % 100L) != 0L || (year % 400L) == 0L)
        }

        actual fun parse(text: CharSequence): Year {
            val s = text.toString()
            val y = s.toIntOrNull()
                ?: throw DateTimeParseException("Text '$s' could not be parsed as Year", s, 0)
            return try {
                of(y)
            } catch (ex: DateTimeException) {
                throw DateTimeParseException("Text '$s' could not be parsed as Year", s, 0)
            }
        }

        actual fun from(temporal: TemporalAccessor): Year {
            if (temporal is Year) return temporal
            try {
                return of(ChronoField.YEAR.getFrom(temporal).toInt())
            } catch (ex: DateTimeException) {
                throw DateTimeException("Unable to obtain Year from TemporalAccessor: $temporal", ex)
            }
        }
    }
}
