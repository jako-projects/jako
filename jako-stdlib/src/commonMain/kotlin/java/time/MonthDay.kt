@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster

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
