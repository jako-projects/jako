@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster

expect enum class Month : TemporalAccessor, TemporalAdjuster {
    JANUARY,
    FEBRUARY,
    MARCH,
    APRIL,
    MAY,
    JUNE,
    JULY,
    AUGUST,
    SEPTEMBER,
    OCTOBER,
    NOVEMBER,
    DECEMBER;

    val value: Int
    fun getValue(): Int
    fun plus(months: Long): Month
    fun minus(months: Long): Month
    fun length(leapYear: Boolean): Int
    fun minLength(): Int
    fun maxLength(): Int
    fun firstDayOfYear(leapYear: Boolean): Int
    override fun adjustInto(temporal: Temporal): Temporal

    companion object {
        fun of(month: Int): Month
        fun from(temporal: TemporalAccessor): Month
    }
}
