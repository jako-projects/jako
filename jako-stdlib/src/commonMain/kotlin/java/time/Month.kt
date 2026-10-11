@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.time.temporal.TemporalAccessor

expect enum class Month : TemporalAccessor {
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

    companion object {
        fun of(month: Int): Month
    }
}
