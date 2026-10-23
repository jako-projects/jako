@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time.temporal

import java.time.DayOfWeek

/**
 * Common and useful TemporalAdjuster implementations.
 */
expect class TemporalAdjusters {
    companion object {
        fun firstDayOfMonth(): TemporalAdjuster
        fun lastDayOfMonth(): TemporalAdjuster
        fun firstDayOfNextMonth(): TemporalAdjuster
        fun firstDayOfYear(): TemporalAdjuster
        fun lastDayOfYear(): TemporalAdjuster
        fun firstDayOfNextYear(): TemporalAdjuster
        fun firstInMonth(dayOfWeek: DayOfWeek): TemporalAdjuster
        fun lastInMonth(dayOfWeek: DayOfWeek): TemporalAdjuster
        fun dayOfWeekInMonth(ordinal: Int, dayOfWeek: DayOfWeek): TemporalAdjuster
        fun next(dayOfWeek: DayOfWeek): TemporalAdjuster
        fun nextOrSame(dayOfWeek: DayOfWeek): TemporalAdjuster
        fun previous(dayOfWeek: DayOfWeek): TemporalAdjuster
        fun previousOrSame(dayOfWeek: DayOfWeek): TemporalAdjuster
    }
}
