@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

expect class LocalDate : TemporalAccessor, Serializable, Comparable<LocalDate> {
    val year: Int
    val month: Month
    val monthValue: Int
    val dayOfMonth: Int

    fun getYear(): Int
    fun getMonth(): Month
    fun getMonthValue(): Int
    fun getDayOfMonth(): Int
    fun isLeapYear(): Boolean
    fun toEpochDay(): Long
    fun plusYears(yearsToAdd: Long): LocalDate
    fun plusMonths(monthsToAdd: Long): LocalDate
    fun plusWeeks(weeksToAdd: Long): LocalDate
    fun plusDays(daysToAdd: Long): LocalDate
    fun minusYears(yearsToSubtract: Long): LocalDate
    fun minusMonths(monthsToSubtract: Long): LocalDate
    fun minusWeeks(weeksToSubtract: Long): LocalDate
    fun minusDays(daysToSubtract: Long): LocalDate
    // [Nullability Decision] time is non-null, matching JDK 8 LocalDate.atTime
    fun atTime(time: LocalTime): LocalDateTime
    fun atTime(hour: Int, minute: Int): LocalDateTime
    fun atTime(hour: Int, minute: Int, second: Int): LocalDateTime
    fun atTime(hour: Int, minute: Int, second: Int, nanoOfSecond: Int): LocalDateTime
    // [Nullability Decision] formatter is non-null, matching JDK 8 LocalDate.format
    fun format(formatter: DateTimeFormatter): String
    fun isAfter(other: LocalDate): Boolean
    fun isBefore(other: LocalDate): Boolean
    override fun compareTo(other: LocalDate): Int

    companion object {
        val MIN: LocalDate
        val MAX: LocalDate
        fun now(): LocalDate
        // [Nullability Decision] zone is non-null, matching JDK 8 LocalDate.now(ZoneId)
        fun now(zone: ZoneId): LocalDate
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
