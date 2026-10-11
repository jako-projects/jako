@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.temporal.TemporalAmount

expect class Period : TemporalAmount, Serializable {
    val years: Int
    val months: Int
    val days: Int

    fun getYears(): Int
    fun getMonths(): Int
    fun getDays(): Int
    fun isZero(): Boolean
    fun isNegative(): Boolean
    // [Nullability Decision] amountToAdd is non-null, matching JDK 8 Period.plus(TemporalAmount)
    fun plus(amountToAdd: TemporalAmount): Period
    fun minus(amountToSubtract: TemporalAmount): Period
    fun plusYears(yearsToAdd: Long): Period
    fun plusMonths(monthsToAdd: Long): Period
    fun plusDays(daysToAdd: Long): Period
    fun minusYears(yearsToSubtract: Long): Period
    fun minusMonths(monthsToSubtract: Long): Period
    fun minusDays(daysToSubtract: Long): Period

    companion object {
        val ZERO: Period
        fun of(years: Int, months: Int, days: Int): Period
        fun ofYears(years: Int): Period
        fun ofMonths(months: Int): Period
        fun ofWeeks(weeks: Int): Period
        fun ofDays(days: Int): Period
    }
}
