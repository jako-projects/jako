@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalUnit

/**
 * A day-of-week, such as 'Tuesday'.
 */
expect enum class DayOfWeek : TemporalAccessor, TemporalAdjuster {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    val value: Int
    fun getValue(): Int
    fun plus(days: Long): DayOfWeek
    fun minus(days: Long): DayOfWeek
    override fun adjustInto(temporal: Temporal): Temporal

    companion object {
        fun of(dayOfWeek: Int): DayOfWeek
        fun from(temporal: TemporalAccessor): DayOfWeek
    }
}

/**
 * A year in the ISO-8601 calendar system, such as 2007.
 */
expect class Year : Temporal, TemporalAdjuster, Comparable<Year>, Serializable {
    val value: Int
    val isLeap: Boolean

    fun getValue(): Int
    fun isLeap(): Boolean
    fun length(): Int
    fun plus(amountToAdd: Long, unit: TemporalUnit): Year
    fun plusYears(yearsToAdd: Long): Year
    fun minus(amountToSubtract: Long, unit: TemporalUnit): Year
    fun minusYears(yearsToSubtract: Long): Year
    fun until(endExclusive: Temporal, unit: TemporalUnit): Long
    fun atDay(dayOfYear: Int): LocalDate
    fun atMonth(month: Month): YearMonth
    fun atMonth(month: Int): YearMonth
    fun atMonthDay(monthDay: MonthDay): LocalDate
    fun isAfter(other: Year): Boolean
    fun isBefore(other: Year): Boolean
    override fun adjustInto(temporal: Temporal): Temporal
    override fun compareTo(other: Year): Int

    companion object {
        val MIN_VALUE: Int
        val MAX_VALUE: Int
        fun now(): Year
        fun now(zone: ZoneId): Year
        fun now(clock: Clock): Year
        fun of(isoYear: Int): Year
        fun isLeap(year: Long): Boolean
        fun parse(text: CharSequence): Year
        fun from(temporal: TemporalAccessor): Year
    }
}

/**
 * A year-month in the ISO-8601 calendar system, such as 2007-12.
 */
expect class YearMonth : Temporal, TemporalAdjuster, Comparable<YearMonth>, Serializable {
    val year: Int
    val monthValue: Int
    val month: Month
    val isLeapYear: Boolean

    fun getYear(): Int
    fun getMonthValue(): Int
    fun getMonth(): Month
    fun isLeapYear(): Boolean
    fun isValidDay(dayOfMonth: Int): Boolean
    fun lengthOfMonth(): Int
    fun lengthOfYear(): Int
    fun withYear(year: Int): YearMonth
    fun withMonth(month: Int): YearMonth
    fun plus(amountToAdd: Long, unit: TemporalUnit): YearMonth
    fun plusYears(yearsToAdd: Long): YearMonth
    fun plusMonths(monthsToAdd: Long): YearMonth
    fun minus(amountToSubtract: Long, unit: TemporalUnit): YearMonth
    fun minusYears(yearsToSubtract: Long): YearMonth
    fun minusMonths(monthsToSubtract: Long): YearMonth
    fun until(endExclusive: Temporal, unit: TemporalUnit): Long
    fun atDay(dayOfMonth: Int): LocalDate
    fun atEndOfMonth(): LocalDate
    fun isAfter(other: YearMonth): Boolean
    fun isBefore(other: YearMonth): Boolean
    override fun adjustInto(temporal: Temporal): Temporal
    override fun compareTo(other: YearMonth): Int

    companion object {
        fun now(): YearMonth
        fun now(zone: ZoneId): YearMonth
        fun now(clock: Clock): YearMonth
        fun of(year: Int, month: Month): YearMonth
        fun of(year: Int, month: Int): YearMonth
        fun parse(text: CharSequence): YearMonth
        fun from(temporal: TemporalAccessor): YearMonth
    }
}

/**
 * A month-day in the ISO-8601 calendar system, such as --12-03.
 */
expect class MonthDay : TemporalAccessor, TemporalAdjuster, Comparable<MonthDay>, Serializable {
    val monthValue: Int
    val month: Month
    val dayOfMonth: Int

    fun getMonthValue(): Int
    fun getMonth(): Month
    fun getDayOfMonth(): Int
    fun isValidYear(year: Int): Boolean
    fun withMonth(month: Int): MonthDay
    fun with(month: Month): MonthDay
    fun withDayOfMonth(dayOfMonth: Int): MonthDay
    fun atYear(year: Int): LocalDate
    fun isAfter(other: MonthDay): Boolean
    fun isBefore(other: MonthDay): Boolean
    override fun adjustInto(temporal: Temporal): Temporal
    override fun compareTo(other: MonthDay): Int

    companion object {
        fun now(): MonthDay
        fun now(zone: ZoneId): MonthDay
        fun now(clock: Clock): MonthDay
        fun of(month: Month, dayOfMonth: Int): MonthDay
        fun of(month: Int, dayOfMonth: Int): MonthDay
        fun parse(text: CharSequence): MonthDay
        fun from(temporal: TemporalAccessor): MonthDay
    }
}

/**
 * A time with an offset from UTC/Greenwich in the ISO-8601 calendar system, such as 10:15:30+01:00.
 */
expect class OffsetTime : Temporal, TemporalAdjuster, Comparable<OffsetTime>, Serializable {
    val hour: Int
    val minute: Int
    val second: Int
    val nano: Int
    val offset: ZoneOffset

    fun getHour(): Int
    fun getMinute(): Int
    fun getSecond(): Int
    fun getNano(): Int
    fun getOffset(): ZoneOffset
    fun toLocalTime(): LocalTime
    fun withOffsetSameLocal(offset: ZoneOffset): OffsetTime
    fun withOffsetSameInstant(offset: ZoneOffset): OffsetTime
    fun plus(amountToAdd: Long, unit: TemporalUnit): OffsetTime
    fun plusHours(hours: Long): OffsetTime
    fun plusMinutes(minutes: Long): OffsetTime
    fun plusSeconds(seconds: Long): OffsetTime
    fun plusNanos(nanos: Long): OffsetTime
    fun minus(amountToSubtract: Long, unit: TemporalUnit): OffsetTime
    fun minusHours(hours: Long): OffsetTime
    fun minusMinutes(minutes: Long): OffsetTime
    fun minusSeconds(seconds: Long): OffsetTime
    fun minusNanos(nanos: Long): OffsetTime
    fun until(endExclusive: Temporal, unit: TemporalUnit): Long
    fun atDate(date: LocalDate): OffsetDateTime
    fun isAfter(other: OffsetTime): Boolean
    fun isBefore(other: OffsetTime): Boolean
    fun isEqual(other: OffsetTime): Boolean
    override fun adjustInto(temporal: Temporal): Temporal
    override fun compareTo(other: OffsetTime): Int

    companion object {
        val MIN: OffsetTime
        val MAX: OffsetTime
        fun now(): OffsetTime
        fun now(zone: ZoneId): OffsetTime
        fun now(clock: Clock): OffsetTime
        fun of(time: LocalTime, offset: ZoneOffset): OffsetTime
        fun of(hour: Int, minute: Int, second: Int, nanoOfSecond: Int, offset: ZoneOffset): OffsetTime
        fun ofInstant(instant: Instant, zone: ZoneId): OffsetTime
        fun parse(text: CharSequence): OffsetTime
        fun from(temporal: TemporalAccessor): OffsetTime
    }
}

/**
 * A clock providing access to the current instant, date and time using a time-zone.
 */
expect abstract class Clock {
    protected constructor()

    val zone: ZoneId
    abstract fun getZone(): ZoneId
    abstract fun withZone(zone: ZoneId): Clock
    open fun millis(): Long
    abstract fun instant(): Instant

    companion object {
        fun systemUTC(): Clock
        fun systemDefaultZone(): Clock
        fun system(zone: ZoneId): Clock
        fun tickSeconds(zone: ZoneId): Clock
        fun tickMinutes(zone: ZoneId): Clock
        fun tick(baseClock: Clock, tickDuration: Duration): Clock
        fun fixed(fixedInstant: Instant, zone: ZoneId): Clock
        fun offset(baseClock: Clock, offsetDuration: Duration): Clock
    }
}
