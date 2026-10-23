@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalUnit

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
