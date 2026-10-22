@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster

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
