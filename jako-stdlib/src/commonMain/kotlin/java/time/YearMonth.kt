@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalUnit

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
