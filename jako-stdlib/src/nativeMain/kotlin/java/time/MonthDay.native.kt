package java.time

import java.io.Serializable
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster

actual class MonthDay private constructor(
    actual val monthValue: Int,
    actual val dayOfMonth: Int
) : TemporalAccessor, TemporalAdjuster, Comparable<MonthDay>, Serializable {

    actual val month: Month get() = Month.of(monthValue)

    actual fun getMonthValue(): Int = monthValue
    actual fun getMonth(): Month = month
    actual fun getDayOfMonth(): Int = dayOfMonth

    actual fun isValidYear(year: Int): Boolean = !(dayOfMonth == 29 && monthValue == 2 && !isLeapYear(year))

    actual fun withMonth(month: Int): MonthDay = with(Month.of(month))

    actual fun with(month: Month): MonthDay {
        if (month.value == monthValue) return this
        val dom = minOf(dayOfMonth, month.maxLength())
        return MonthDay(month.value, dom)
    }

    actual fun withDayOfMonth(dayOfMonth: Int): MonthDay {
        if (dayOfMonth == this.dayOfMonth) return this
        return of(monthValue, dayOfMonth)
    }

    actual fun atYear(year: Int): LocalDate =
        LocalDate.of(year, monthValue, if (isValidYear(year)) dayOfMonth else 28)

    actual fun isAfter(other: MonthDay): Boolean = compareTo(other) > 0
    actual fun isBefore(other: MonthDay): Boolean = compareTo(other) < 0

    actual override fun adjustInto(temporal: Temporal): Temporal {
        return when (temporal) {
            is LocalDate -> {
                val dom = minOf(dayOfMonth, lengthOfMonth(temporal.year, monthValue))
                LocalDate.of(temporal.year, monthValue, dom)
            }
            is LocalDateTime -> {
                val dom = minOf(dayOfMonth, lengthOfMonth(temporal.year, monthValue))
                LocalDateTime.of(LocalDate.of(temporal.year, monthValue, dom), temporal.toLocalTime())
            }
            else -> throw DateTimeException("Unable to adjust temporal with MonthDay: $temporal")
        }
    }

    actual override fun compareTo(other: MonthDay): Int {
        val cmp = monthValue.compareTo(other.monthValue)
        if (cmp != 0) return cmp
        return dayOfMonth.compareTo(other.dayOfMonth)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MonthDay) return false
        return monthValue == other.monthValue && dayOfMonth == other.dayOfMonth
    }

    override fun hashCode(): Int = (monthValue shl 6) + dayOfMonth

    override fun toString(): String {
        return StringBuilder(10).append("--")
            .append(if (monthValue < 10) "0" else "").append(monthValue)
            .append(if (dayOfMonth < 10) "-0" else "-").append(dayOfMonth)
            .toString()
    }

    actual companion object {
        actual fun now(): MonthDay = now(Clock.systemDefaultZone())
        actual fun now(zone: ZoneId): MonthDay = now(Clock.system(zone))
        actual fun now(clock: Clock): MonthDay {
            val ld = LocalDate.now(clock)
            return of(ld.month, ld.dayOfMonth)
        }

        actual fun of(month: Month, dayOfMonth: Int): MonthDay {
            if (dayOfMonth < 1 || dayOfMonth > month.maxLength()) {
                throw DateTimeException("Illegal value for DayOfMonth field, value $dayOfMonth is not valid for month ${month.name}")
            }
            return MonthDay(month.value, dayOfMonth)
        }

        actual fun of(month: Int, dayOfMonth: Int): MonthDay = of(Month.of(month), dayOfMonth)

        actual fun parse(text: CharSequence): MonthDay {
            val s = text.toString()
            if (s.length != 7 || !s.startsWith("--") || s[4] != '-') {
                throw DateTimeParseException("Text '$s' could not be parsed as MonthDay", s, 0)
            }
            val m = s.substring(2, 4).toIntOrNull()
                ?: throw DateTimeParseException("Text '$s' could not be parsed as MonthDay", s, 2)
            val d = s.substring(5, 7).toIntOrNull()
                ?: throw DateTimeParseException("Text '$s' could not be parsed as MonthDay", s, 5)
            return try {
                of(m, d)
            } catch (ex: DateTimeException) {
                throw DateTimeParseException("Text '$s' could not be parsed as MonthDay", s, 0)
            }
        }

        actual fun from(temporal: TemporalAccessor): MonthDay {
            if (temporal is MonthDay) return temporal
            try {
                return of(
                    ChronoField.MONTH_OF_YEAR.getFrom(temporal).toInt(),
                    ChronoField.DAY_OF_MONTH.getFrom(temporal).toInt()
                )
            } catch (ex: DateTimeException) {
                throw DateTimeException("Unable to obtain MonthDay from TemporalAccessor: $temporal", ex)
            }
        }
    }
}
