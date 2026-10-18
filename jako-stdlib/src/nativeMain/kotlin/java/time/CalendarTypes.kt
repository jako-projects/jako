package java.time

import java.io.Serializable
import java.lang.System
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalUnit
import java.time.temporal.UnsupportedTemporalTypeException

actual enum class DayOfWeek : TemporalAccessor, TemporalAdjuster {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    actual val value: Int get() = ordinal + 1
    actual fun getValue(): Int = value

    actual fun plus(days: Long): DayOfWeek {
        val amount = (days % 7).toInt()
        return values()[(ordinal + (amount + 7)) % 7]
    }

    actual fun minus(days: Long): DayOfWeek = plus(-(days % 7))

    actual override fun adjustInto(temporal: Temporal): Temporal {
        return when (temporal) {
            is LocalDate -> temporal.plusDays((this.value - temporal.dayOfWeek.value).toLong())
            is LocalDateTime -> temporal.plusDays((this.value - temporal.dayOfWeek.value).toLong())
            is OffsetDateTime -> OffsetDateTime.of(temporal.toLocalDateTime().plusDays((this.value - temporal.toLocalDate().dayOfWeek.value).toLong()), temporal.offset)
            is ZonedDateTime -> ZonedDateTime.of(temporal.toLocalDateTime().plusDays((this.value - temporal.toLocalDate().dayOfWeek.value).toLong()), temporal.zone)
            else -> throw DateTimeException("Unable to adjust temporal with DayOfWeek: $temporal")
        }
    }

    actual companion object {
        actual fun of(dayOfWeek: Int): DayOfWeek {
            if (dayOfWeek < 1 || dayOfWeek > 7) {
                throw DateTimeException("Invalid value for DayOfWeek: $dayOfWeek")
            }
            return values()[dayOfWeek - 1]
        }

        actual fun from(temporal: TemporalAccessor): DayOfWeek {
            if (temporal is DayOfWeek) return temporal
            try {
                return of(ChronoField.DAY_OF_WEEK.getFrom(temporal).toInt())
            } catch (ex: DateTimeException) {
                throw DateTimeException("Unable to obtain DayOfWeek from TemporalAccessor: $temporal", ex)
            }
        }
    }
}

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

actual class OffsetTime private constructor(
    private val time: LocalTime,
    actual val offset: ZoneOffset
) : Temporal, TemporalAdjuster, Comparable<OffsetTime>, Serializable {

    actual val hour: Int get() = time.hour
    actual val minute: Int get() = time.minute
    actual val second: Int get() = time.second
    actual val nano: Int get() = time.nano

    actual fun getHour(): Int = time.hour
    actual fun getMinute(): Int = time.minute
    actual fun getSecond(): Int = time.second
    actual fun getNano(): Int = time.nano
    actual fun getOffset(): ZoneOffset = offset
    actual fun toLocalTime(): LocalTime = time

    actual fun withOffsetSameLocal(offset: ZoneOffset): OffsetTime =
        if (offset == this.offset) this else OffsetTime(time, offset)

    actual fun withOffsetSameInstant(offset: ZoneOffset): OffsetTime {
        if (offset == this.offset) return this
        val diffSecs = offset.totalSeconds - this.offset.totalSeconds
        return OffsetTime(time.plusSeconds(diffSecs.toLong()), offset)
    }

    actual fun plus(amountToAdd: Long, unit: TemporalUnit): OffsetTime {
        if (unit is ChronoUnit) {
            return OffsetTime(time.plus(amountToAdd, unit), offset)
        }
        return unit.addTo(this, amountToAdd)
    }

    actual fun plusHours(hours: Long): OffsetTime = OffsetTime(time.plusHours(hours), offset)
    actual fun plusMinutes(minutes: Long): OffsetTime = OffsetTime(time.plusMinutes(minutes), offset)
    actual fun plusSeconds(seconds: Long): OffsetTime = OffsetTime(time.plusSeconds(seconds), offset)
    actual fun plusNanos(nanos: Long): OffsetTime = OffsetTime(time.plusNanos(nanos), offset)

    actual fun minus(amountToSubtract: Long, unit: TemporalUnit): OffsetTime {
        return if (amountToSubtract == Long.MIN_VALUE) {
            plus(Long.MAX_VALUE, unit).plus(1, unit)
        } else {
            plus(-amountToSubtract, unit)
        }
    }

    actual fun minusHours(hours: Long): OffsetTime = OffsetTime(time.minusHours(hours), offset)
    actual fun minusMinutes(minutes: Long): OffsetTime = OffsetTime(time.minusMinutes(minutes), offset)
    actual fun minusSeconds(seconds: Long): OffsetTime = OffsetTime(time.minusSeconds(seconds), offset)
    actual fun minusNanos(nanos: Long): OffsetTime = OffsetTime(time.minusNanos(nanos), offset)

    actual fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        val end = from(endExclusive)
        if (unit is ChronoUnit) {
            val nanosUntil = end.toEpochNano() - toEpochNano()
            return when (unit) {
                ChronoUnit.NANOS -> nanosUntil
                ChronoUnit.MICROS -> nanosUntil / 1000L
                ChronoUnit.MILLIS -> nanosUntil / 1_000_000L
                ChronoUnit.SECONDS -> nanosUntil / NANOS_PER_SECOND
                ChronoUnit.MINUTES -> nanosUntil / NANOS_PER_MINUTE
                ChronoUnit.HOURS -> nanosUntil / NANOS_PER_HOUR
                ChronoUnit.HALF_DAYS -> nanosUntil / (12 * NANOS_PER_HOUR)
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.between(this, end)
    }

    actual fun atDate(date: LocalDate): OffsetDateTime = OffsetDateTime.of(date, time, offset)

    private fun toEpochNano(): Long = time.toNanoOfDay() - offset.totalSeconds * NANOS_PER_SECOND

    actual fun isAfter(other: OffsetTime): Boolean = toEpochNano() > other.toEpochNano()
    actual fun isBefore(other: OffsetTime): Boolean = toEpochNano() < other.toEpochNano()
    actual fun isEqual(other: OffsetTime): Boolean = toEpochNano() == other.toEpochNano()

    actual override fun adjustInto(temporal: Temporal): Temporal {
        return when (temporal) {
            is OffsetTime -> this
            is OffsetDateTime -> OffsetDateTime.of(temporal.toLocalDate(), time, offset)
            else -> throw DateTimeException("Unable to adjust temporal with OffsetTime: $temporal")
        }
    }

    actual override fun compareTo(other: OffsetTime): Int {
        if (offset == other.offset) {
            return time.compareTo(other.time)
        }
        val cmp = toEpochNano().compareTo(other.toEpochNano())
        if (cmp != 0) return cmp
        return time.compareTo(other.time)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OffsetTime) return false
        return time == other.time && offset == other.offset
    }

    override fun hashCode(): Int = time.hashCode() xor offset.hashCode()

    override fun toString(): String = time.toString() + offset.toString()

    actual companion object {
        actual val MIN: OffsetTime = OffsetTime(LocalTime.MIN, ZoneOffset.MAX)
        actual val MAX: OffsetTime = OffsetTime(LocalTime.MAX, ZoneOffset.MIN)

        actual fun now(): OffsetTime = now(Clock.systemDefaultZone())
        actual fun now(zone: ZoneId): OffsetTime = now(Clock.system(zone))
        actual fun now(clock: Clock): OffsetTime = ofInstant(clock.instant(), clock.zone)

        actual fun of(time: LocalTime, offset: ZoneOffset): OffsetTime = OffsetTime(time, offset)

        actual fun of(hour: Int, minute: Int, second: Int, nanoOfSecond: Int, offset: ZoneOffset): OffsetTime =
            OffsetTime(LocalTime.of(hour, minute, second, nanoOfSecond), offset)

        actual fun ofInstant(instant: Instant, zone: ZoneId): OffsetTime {
            val offset = zone.offsetAt(instant)
            var secsOfDay = (instant.epochSecond + offset.totalSeconds).mod(SECONDS_PER_DAY)
            if (secsOfDay < 0) secsOfDay += SECONDS_PER_DAY
            val time = LocalTime.ofNanoOfDay(secsOfDay * NANOS_PER_SECOND + instant.nano)
            return OffsetTime(time, offset)
        }

        actual fun parse(text: CharSequence): OffsetTime {
            val s = text.toString()
            val offsetStart = findOffsetStart(s)
                ?: throw DateTimeParseException("Text '$s' could not be parsed as OffsetTime", s, 0)
            val time = parseIsoLocalTimeString(s.substring(0, offsetStart))
            val offsetStr = s.substring(offsetStart)
            val offset = try {
                if (offsetStr == "Z") ZoneOffset.UTC else ZoneOffset.of(offsetStr)
            } catch (ex: DateTimeException) {
                throw DateTimeParseException("Text '$s' could not be parsed as OffsetTime", s, offsetStart)
            }
            return OffsetTime(time, offset)
        }

        actual fun from(temporal: TemporalAccessor): OffsetTime {
            if (temporal is OffsetTime) return temporal
            if (temporal is OffsetDateTime) return OffsetTime(temporal.toLocalTime(), temporal.offset)
            if (temporal is ZonedDateTime) return OffsetTime(temporal.toLocalTime(), temporal.offset)
            throw DateTimeException("Unable to obtain OffsetTime from TemporalAccessor: $temporal")
        }
    }
}

actual abstract class Clock protected actual constructor() {
    actual val zone: ZoneId get() = getZone()
    actual abstract fun getZone(): ZoneId
    actual abstract fun withZone(zone: ZoneId): Clock
    actual open fun millis(): Long = instant().toEpochMilli()
    actual abstract fun instant(): Instant

    actual companion object {
        actual fun systemUTC(): Clock = SystemClock(ZoneOffset.UTC)
        actual fun systemDefaultZone(): Clock = SystemClock(ZoneId.systemDefault())
        actual fun system(zone: ZoneId): Clock = SystemClock(zone)

        actual fun tickSeconds(zone: ZoneId): Clock = TickClock(system(zone), NANOS_PER_SECOND)
        actual fun tickMinutes(zone: ZoneId): Clock = TickClock(system(zone), NANOS_PER_MINUTE)

        actual fun tick(baseClock: Clock, tickDuration: Duration): Clock {
            if (tickDuration.isNegative()) {
                throw IllegalArgumentException("Tick duration must not be negative")
            }
            val tickNanos = tickDuration.toNanos()
            if (tickNanos <= 1L) return baseClock
            return TickClock(baseClock, tickNanos)
        }

        actual fun fixed(fixedInstant: Instant, zone: ZoneId): Clock = FixedClock(fixedInstant, zone)

        actual fun offset(baseClock: Clock, offsetDuration: Duration): Clock {
            if (offsetDuration.isZero()) return baseClock
            return OffsetClock(baseClock, offsetDuration)
        }
    }
}

private class SystemClock(private val clockZone: ZoneId) : Clock(), Serializable {
    override fun getZone(): ZoneId = clockZone
    override fun withZone(zone: ZoneId): Clock = if (zone == clockZone) this else SystemClock(zone)
    override fun millis(): Long = System.currentTimeMillis()
    override fun instant(): Instant = Instant.ofEpochMilli(millis())
    override fun equals(other: Any?): Boolean = other is SystemClock && clockZone == other.clockZone
    override fun hashCode(): Int = clockZone.hashCode() + 1
    override fun toString(): String = "SystemClock[$clockZone]"
}

private class FixedClock(private val fixedInstant: Instant, private val clockZone: ZoneId) : Clock(), Serializable {
    override fun getZone(): ZoneId = clockZone
    override fun withZone(zone: ZoneId): Clock = if (zone == clockZone) this else FixedClock(fixedInstant, zone)
    override fun millis(): Long = fixedInstant.toEpochMilli()
    override fun instant(): Instant = fixedInstant
    override fun equals(other: Any?): Boolean =
        other is FixedClock && fixedInstant == other.fixedInstant && clockZone == other.clockZone
    override fun hashCode(): Int = fixedInstant.hashCode() xor clockZone.hashCode()
    override fun toString(): String = "FixedClock[$fixedInstant,$clockZone]"
}

private class OffsetClock(private val baseClock: Clock, private val offset: Duration) : Clock(), Serializable {
    override fun getZone(): ZoneId = baseClock.getZone()
    override fun withZone(zone: ZoneId): Clock =
        if (zone == baseClock.getZone()) this else OffsetClock(baseClock.withZone(zone), offset)
    override fun millis(): Long = baseClock.millis() + offset.toMillis()
    override fun instant(): Instant = baseClock.instant().plusSeconds(offset.seconds).plusNanos(offset.nano.toLong())
    override fun equals(other: Any?): Boolean =
        other is OffsetClock && baseClock == other.baseClock && offset == other.offset
    override fun hashCode(): Int = baseClock.hashCode() xor offset.hashCode()
    override fun toString(): String = "OffsetClock[$baseClock,$offset]"
}

private class TickClock(private val baseClock: Clock, private val tickNanos: Long) : Clock(), Serializable {
    override fun getZone(): ZoneId = baseClock.getZone()
    override fun withZone(zone: ZoneId): Clock =
        if (zone == baseClock.getZone()) this else TickClock(baseClock.withZone(zone), tickNanos)
    override fun millis(): Long {
        val millis = baseClock.millis()
        val tickMillis = tickNanos / 1_000_000L
        return if (tickMillis <= 1L) millis else millis - millis.mod(tickMillis)
    }
    override fun instant(): Instant {
        if ((tickNanos % 1_000_000L) == 0L) {
            val millis = baseClock.millis()
            val tickMillis = tickNanos / 1_000_000L
            return Instant.ofEpochMilli(millis - millis.mod(tickMillis))
        }
        val instant = baseClock.instant()
        val nanos = instant.nano.toLong()
        return instant.minusNanos(nanos.mod(tickNanos))
    }
    override fun equals(other: Any?): Boolean =
        other is TickClock && baseClock == other.baseClock && tickNanos == other.tickNanos
    override fun hashCode(): Int = baseClock.hashCode() xor (tickNanos xor (tickNanos ushr 32)).toInt()
    override fun toString(): String = "TickClock[$baseClock,${Duration.ofNanos(tickNanos)}]"
}
