@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAmount
import java.time.temporal.TemporalField
import java.time.temporal.TemporalUnit
import java.time.temporal.ValueRange

expect class LocalDate : Temporal, TemporalAdjuster, Serializable, Comparable<LocalDate> {
    val year: Int
    val month: Month
    val monthValue: Int
    val dayOfMonth: Int
    val dayOfYear: Int
    val dayOfWeek: DayOfWeek

    fun isSupported(field: TemporalField?): Boolean
    fun isSupported(unit: TemporalUnit?): Boolean
    fun range(field: TemporalField): ValueRange
    fun get(field: TemporalField): Int
    fun getLong(field: TemporalField): Long
    fun getYear(): Int
    fun getMonth(): Month
    fun getMonthValue(): Int
    fun getDayOfMonth(): Int
    fun getDayOfYear(): Int
    fun getDayOfWeek(): DayOfWeek
    fun isLeapYear(): Boolean
    fun lengthOfMonth(): Int
    fun lengthOfYear(): Int
    fun toEpochDay(): Long
    fun with(adjuster: TemporalAdjuster): LocalDate
    fun with(field: TemporalField, newValue: Long): LocalDate
    fun withYear(year: Int): LocalDate
    fun withMonth(month: Int): LocalDate
    fun withDayOfMonth(dayOfMonth: Int): LocalDate
    fun withDayOfYear(dayOfYear: Int): LocalDate
    fun plus(amountToAdd: TemporalAmount): LocalDate
    fun plus(amountToAdd: Long, unit: TemporalUnit): LocalDate
    fun plusYears(yearsToAdd: Long): LocalDate
    fun plusMonths(monthsToAdd: Long): LocalDate
    fun plusWeeks(weeksToAdd: Long): LocalDate
    fun plusDays(daysToAdd: Long): LocalDate
    fun minus(amountToSubtract: TemporalAmount): LocalDate
    fun minus(amountToSubtract: Long, unit: TemporalUnit): LocalDate
    fun minusYears(yearsToSubtract: Long): LocalDate
    fun minusMonths(monthsToSubtract: Long): LocalDate
    fun minusWeeks(weeksToSubtract: Long): LocalDate
    fun minusDays(daysToSubtract: Long): LocalDate
    fun until(endDateExclusive: LocalDate): Period
    fun until(endExclusive: Temporal, unit: TemporalUnit): Long
    // [Nullability Decision] time is non-null, matching JDK 8 LocalDate.atTime
    fun atTime(time: LocalTime): LocalDateTime
    fun atTime(hour: Int, minute: Int): LocalDateTime
    fun atTime(hour: Int, minute: Int, second: Int): LocalDateTime
    fun atTime(hour: Int, minute: Int, second: Int, nanoOfSecond: Int): LocalDateTime
    fun atStartOfDay(): LocalDateTime
    fun atStartOfDay(zone: ZoneId): ZonedDateTime
    // [Nullability Decision] formatter is non-null, matching JDK 8 LocalDate.format
    fun format(formatter: DateTimeFormatter): String
    fun isAfter(other: LocalDate): Boolean
    fun isBefore(other: LocalDate): Boolean
    override fun adjustInto(temporal: Temporal): Temporal
    override fun compareTo(other: LocalDate): Int

    companion object {
        val MIN: LocalDate
        val MAX: LocalDate
        fun now(): LocalDate
        // [Nullability Decision] zone is non-null, matching JDK 8 LocalDate.now(ZoneId)
        fun now(zone: ZoneId): LocalDate
        fun now(clock: Clock): LocalDate
        fun of(year: Int, month: Int, dayOfMonth: Int): LocalDate
        fun of(year: Int, month: Month, dayOfMonth: Int): LocalDate
        fun ofYearDay(year: Int, dayOfYear: Int): LocalDate
        fun ofEpochDay(epochDay: Long): LocalDate
        // [Nullability Decision] text is non-null, matching JDK 8 LocalDate.parse
        fun parse(text: CharSequence): LocalDate
        fun parse(text: CharSequence, formatter: DateTimeFormatter): LocalDate
        fun from(temporal: TemporalAccessor): LocalDate
    }
}
