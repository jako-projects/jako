package java.time

import java.io.Serializable
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

actual class LocalDate private constructor(
    actual val year: Int,
    private val monthOfYear: Int,
    actual val dayOfMonth: Int
) : TemporalAccessor, Serializable, Comparable<LocalDate> {

    actual val month: Month get() = Month.of(monthOfYear)
    actual val monthValue: Int get() = monthOfYear

    actual fun getYear(): Int = year
    actual fun getMonth(): Month = month
    actual fun getMonthValue(): Int = monthOfYear
    actual fun getDayOfMonth(): Int = dayOfMonth
    actual fun isLeapYear(): Boolean = isLeapYear(year)

    actual fun toEpochDay(): Long {
        val y = year.toLong()
        val m = monthOfYear.toLong()
        var total = 0L
        total += 365 * y
        if (y >= 0) {
            total += (y + 3) / 4 - (y + 99) / 100 + (y + 399) / 400
        } else {
            total -= y / -4 - y / -100 + y / -400
        }
        total += ((367 * m - 362) / 12)
        total += dayOfMonth - 1
        if (m > 2) {
            total--
            if (!isLeapYear(year)) {
                total--
            }
        }
        return total - DAYS_0000_TO_1970
    }

    actual fun plusYears(yearsToAdd: Long): LocalDate {
        if (yearsToAdd == 0L) return this
        val newYear = checkYear(year + yearsToAdd)
        return resolvePreviousValid(newYear, monthOfYear, dayOfMonth)
    }

    actual fun plusMonths(monthsToAdd: Long): LocalDate {
        if (monthsToAdd == 0L) return this
        val monthCount = year * 12L + (monthOfYear - 1)
        val calcMonths = addExact(monthCount, monthsToAdd)
        val newYear = checkYear(calcMonths.floorDiv(12L))
        val newMonth = calcMonths.mod(12L).toInt() + 1
        return resolvePreviousValid(newYear, newMonth, dayOfMonth)
    }

    actual fun plusWeeks(weeksToAdd: Long): LocalDate = plusDays(multiplyLong(weeksToAdd, 7))

    actual fun plusDays(daysToAdd: Long): LocalDate {
        if (daysToAdd == 0L) return this
        return ofEpochDay(addExact(toEpochDay(), daysToAdd))
    }

    actual fun minusYears(yearsToSubtract: Long): LocalDate {
        return if (yearsToSubtract == Long.MIN_VALUE) {
            plusYears(Long.MAX_VALUE).plusYears(1)
        } else {
            plusYears(-yearsToSubtract)
        }
    }

    actual fun minusMonths(monthsToSubtract: Long): LocalDate {
        return if (monthsToSubtract == Long.MIN_VALUE) {
            plusMonths(Long.MAX_VALUE).plusMonths(1)
        } else {
            plusMonths(-monthsToSubtract)
        }
    }

    actual fun minusWeeks(weeksToSubtract: Long): LocalDate {
        return if (weeksToSubtract == Long.MIN_VALUE) {
            plusWeeks(Long.MAX_VALUE).plusWeeks(1)
        } else {
            plusWeeks(-weeksToSubtract)
        }
    }

    actual fun minusDays(daysToSubtract: Long): LocalDate {
        return if (daysToSubtract == Long.MIN_VALUE) {
            plusDays(Long.MAX_VALUE).plusDays(1)
        } else {
            plusDays(-daysToSubtract)
        }
    }

    actual fun atTime(time: LocalTime): LocalDateTime = LocalDateTime.of(this, time)

    actual fun atTime(hour: Int, minute: Int): LocalDateTime = atTime(LocalTime.of(hour, minute))

    actual fun atTime(hour: Int, minute: Int, second: Int): LocalDateTime =
        atTime(LocalTime.of(hour, minute, second))

    actual fun atTime(hour: Int, minute: Int, second: Int, nanoOfSecond: Int): LocalDateTime =
        atTime(LocalTime.of(hour, minute, second, nanoOfSecond))

    actual fun format(formatter: DateTimeFormatter): String = formatter.format(this)

    actual fun isAfter(other: LocalDate): Boolean = compareTo(other) > 0
    actual fun isBefore(other: LocalDate): Boolean = compareTo(other) < 0

    actual override fun compareTo(other: LocalDate): Int {
        var cmp = year.compareTo(other.year)
        if (cmp == 0) {
            cmp = monthOfYear.compareTo(other.monthOfYear)
            if (cmp == 0) {
                cmp = dayOfMonth.compareTo(other.dayOfMonth)
            }
        }
        return cmp
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocalDate) return false
        return year == other.year && monthOfYear == other.monthOfYear && dayOfMonth == other.dayOfMonth
    }

    override fun hashCode(): Int {
        val yearValue = year
        return (yearValue and -0x800) xor ((yearValue shl 11) + (monthOfYear shl 6) + dayOfMonth)
    }

    override fun toString(): String = formatIsoDate(year, monthOfYear, dayOfMonth)

    actual companion object {
        actual val MIN: LocalDate = LocalDate(YEAR_MIN, 1, 1)
        actual val MAX: LocalDate = LocalDate(YEAR_MAX, 12, 31)

        actual fun now(): LocalDate = now(ZoneId.systemDefault())

        actual fun now(zone: ZoneId): LocalDate {
            val instant = Instant.now()
            val offset = zone.offsetAt(instant)
            val localSecond = instant.epochSecond + offset.totalSeconds
            return ofEpochDay(localSecond.floorDiv(SECONDS_PER_DAY))
        }

        actual fun of(year: Int, month: Int, dayOfMonth: Int): LocalDate {
            checkYear(year.toLong())
            if (month < 1 || month > 12) {
                throw DateTimeException("Invalid value for MonthOfYear (valid values 1 - 12): $month")
            }
            if (dayOfMonth < 1 || dayOfMonth > 31) {
                throw DateTimeException("Invalid value for DayOfMonth (valid values 1 - 28/31): $dayOfMonth")
            }
            if (dayOfMonth > lengthOfMonth(year, month)) {
                if (dayOfMonth == 29) {
                    throw DateTimeException("Invalid date 'February 29' as '$year' is not a leap year")
                }
                throw DateTimeException("Invalid date '${Month.of(month).name} $dayOfMonth'")
            }
            return LocalDate(year, month, dayOfMonth)
        }

        actual fun of(year: Int, month: Month, dayOfMonth: Int): LocalDate = of(year, month.value, dayOfMonth)

        actual fun ofYearDay(year: Int, dayOfYear: Int): LocalDate {
            checkYear(year.toLong())
            val leap = isLeapYear(year)
            if (dayOfYear < 1 || dayOfYear > (if (leap) 366 else 365)) {
                throw DateTimeException("Invalid value for DayOfYear (valid values 1 - 365/366): $dayOfYear")
            }
            if (dayOfYear == 366 && !leap) {
                throw DateTimeException("Invalid date 'DayOfYear 366' as '$year' is not a leap year")
            }
            var month = (dayOfYear - 1) / 31 + 1
            var monthEnd = dayOfYearFromMonth(year, month + 1) - 1
            if (dayOfYear > monthEnd) {
                month++
            }
            val day = dayOfYear - dayOfYearFromMonth(year, month) + 1
            return LocalDate(year, month, day)
        }

        actual fun ofEpochDay(epochDay: Long): LocalDate {
            var zeroDay = epochDay + DAYS_0000_TO_1970
            zeroDay -= 60
            var adjust = 0L
            if (zeroDay < 0) {
                val adjustCycles = (zeroDay + 1) / DAYS_PER_CYCLE - 1
                adjust = adjustCycles * 400
                zeroDay += -adjustCycles * DAYS_PER_CYCLE
            }
            var yearEst = (400 * zeroDay + 591) / DAYS_PER_CYCLE
            var doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400)
            if (doyEst < 0) {
                yearEst--
                doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400)
            }
            yearEst += adjust
            val marchDoy0 = doyEst.toInt()
            val marchMonth0 = (marchDoy0 * 5 + 2) / 153
            val month = (marchMonth0 + 2) % 12 + 1
            val dom = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1
            yearEst += marchMonth0 / 10
            val year = checkYear(yearEst)
            return LocalDate(year, month, dom)
        }

        actual fun parse(text: CharSequence): LocalDate = parse(text, DateTimeFormatter.ISO_LOCAL_DATE)

        actual fun parse(text: CharSequence, formatter: DateTimeFormatter): LocalDate {
            return from(formatter.parse(text))
        }

        actual fun from(temporal: TemporalAccessor): LocalDate {
            if (temporal is LocalDate) return temporal
            if (temporal is LocalDateTime) return temporal.toLocalDate()
            if (temporal is OffsetDateTime) return temporal.toLocalDate()
            if (temporal is ZonedDateTime) return temporal.toLocalDate()
            throw DateTimeException("Unable to obtain LocalDate from TemporalAccessor: $temporal")
        }

        private fun resolvePreviousValid(year: Int, month: Int, day: Int): LocalDate {
            val dom = when (month) {
                2 -> minOf(day, if (isLeapYear(year)) 29 else 28)
                4, 6, 9, 11 -> minOf(day, 30)
                else -> day
            }
            return LocalDate(year, month, dom)
        }

        private fun dayOfYearFromMonth(year: Int, month: Int): Int {
            val leap = if (isLeapYear(year)) 1 else 0
            return when (month) {
                1 -> 1
                2 -> 32
                3 -> 60 + leap
                4 -> 91 + leap
                5 -> 121 + leap
                6 -> 152 + leap
                7 -> 182 + leap
                8 -> 213 + leap
                9 -> 244 + leap
                10 -> 274 + leap
                11 -> 305 + leap
                12 -> 335 + leap
                else -> 366 + leap
            }
        }

        private fun multiplyLong(a: Long, b: Long): Long {
            if (a == 0L || b == 0L) return 0L
            val r = a * b
            if (r / b != a) {
                throw ArithmeticException("long overflow")
            }
            return r
        }
    }
}
