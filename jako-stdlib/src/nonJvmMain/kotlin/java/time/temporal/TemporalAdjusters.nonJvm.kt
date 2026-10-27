package java.time.temporal

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.addExact
import java.time.isLeapYear
import java.time.lengthOfMonth

actual class TemporalAdjusters private constructor() {
    actual companion object {
        actual fun firstDayOfMonth(): TemporalAdjuster = TemporalAdjuster { temporal ->
            when (temporal) {
                is LocalDate -> temporal.withDayOfMonth(1)
                is LocalDateTime -> temporal.withDayOfMonth(1)
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for firstDayOfMonth: $temporal")
            }
        }

        actual fun lastDayOfMonth(): TemporalAdjuster = TemporalAdjuster { temporal ->
            when (temporal) {
                is LocalDate -> temporal.withDayOfMonth(temporal.lengthOfMonth())
                is LocalDateTime -> temporal.withDayOfMonth(temporal.toLocalDate().lengthOfMonth())
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for lastDayOfMonth: $temporal")
            }
        }

        actual fun firstDayOfNextMonth(): TemporalAdjuster = TemporalAdjuster { temporal ->
            when (temporal) {
                is LocalDate -> temporal.withDayOfMonth(1).plusMonths(1)
                is LocalDateTime -> temporal.withDayOfMonth(1).plusMonths(1)
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for firstDayOfNextMonth: $temporal")
            }
        }

        actual fun firstDayOfYear(): TemporalAdjuster = TemporalAdjuster { temporal ->
            when (temporal) {
                is LocalDate -> temporal.withDayOfYear(1)
                is LocalDateTime -> temporal.withDayOfYear(1)
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for firstDayOfYear: $temporal")
            }
        }

        actual fun lastDayOfYear(): TemporalAdjuster = TemporalAdjuster { temporal ->
            when (temporal) {
                is LocalDate -> temporal.withDayOfYear(temporal.lengthOfYear())
                is LocalDateTime -> temporal.withDayOfYear(temporal.toLocalDate().lengthOfYear())
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for lastDayOfYear: $temporal")
            }
        }

        actual fun firstDayOfNextYear(): TemporalAdjuster = TemporalAdjuster { temporal ->
            when (temporal) {
                is LocalDate -> temporal.withDayOfYear(1).plusYears(1)
                is LocalDateTime -> temporal.withDayOfYear(1).plusYears(1)
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for firstDayOfNextYear: $temporal")
            }
        }

        actual fun firstInMonth(dayOfWeek: DayOfWeek): TemporalAdjuster =
            dayOfWeekInMonth(1, dayOfWeek)

        actual fun lastInMonth(dayOfWeek: DayOfWeek): TemporalAdjuster =
            dayOfWeekInMonth(-1, dayOfWeek)

        actual fun dayOfWeekInMonth(ordinal: Int, dayOfWeek: DayOfWeek): TemporalAdjuster {
            if (ordinal == 0) {
                return TemporalAdjuster { temporal ->
                    val firstOfCurrent = firstDayOfMonth().adjustInto(temporal)
                    previous(dayOfWeek).adjustInto(firstOfCurrent)
                }
            }
            if (ordinal > 0) {
                return TemporalAdjuster { temporal ->
                    val firstOfCurrent = firstDayOfMonth().adjustInto(temporal)
                    val firstDow = nextOrSame(dayOfWeek).adjustInto(firstOfCurrent)
                    val daysToAdd = (ordinal.toLong() - 1L) * 7L
                    when (firstDow) {
                        is LocalDate -> firstDow.plusDays(daysToAdd)
                        is LocalDateTime -> firstDow.plusDays(daysToAdd)
                        else -> ChronoUnit.DAYS.addTo(firstDow, daysToAdd)
                    }
                }
            }
            return TemporalAdjuster { temporal ->
                val lastOfCurrent = lastDayOfMonth().adjustInto(temporal)
                val lastDow = previousOrSame(dayOfWeek).adjustInto(lastOfCurrent)
                val daysToSubtract = (-ordinal.toLong() - 1L) * 7L
                when (lastDow) {
                    is LocalDate -> lastDow.minusDays(daysToSubtract)
                    is LocalDateTime -> lastDow.minusDays(daysToSubtract)
                    else -> ChronoUnit.DAYS.addTo(lastDow, -daysToSubtract)
                }
            }
        }

        actual fun next(dayOfWeek: DayOfWeek): TemporalAdjuster = TemporalAdjuster { temporal ->
            val targetDow = dayOfWeek.value
            when (temporal) {
                is LocalDate -> {
                    val calDow = temporal.dayOfWeek.value
                    val daysDiff = calDow - targetDow
                    temporal.plusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                }
                is LocalDateTime -> {
                    val calDow = temporal.dayOfWeek.value
                    val daysDiff = calDow - targetDow
                    temporal.plusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                }
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for next: $temporal")
            }
        }

        actual fun nextOrSame(dayOfWeek: DayOfWeek): TemporalAdjuster = TemporalAdjuster { temporal ->
            val targetDow = dayOfWeek.value
            when (temporal) {
                is LocalDate -> {
                    val calDow = temporal.dayOfWeek.value
                    if (calDow == targetDow) temporal else {
                        val daysDiff = calDow - targetDow
                        temporal.plusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                    }
                }
                is LocalDateTime -> {
                    val calDow = temporal.dayOfWeek.value
                    if (calDow == targetDow) temporal else {
                        val daysDiff = calDow - targetDow
                        temporal.plusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                    }
                }
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for nextOrSame: $temporal")
            }
        }

        actual fun previous(dayOfWeek: DayOfWeek): TemporalAdjuster = TemporalAdjuster { temporal ->
            val targetDow = dayOfWeek.value
            when (temporal) {
                is LocalDate -> {
                    val calDow = temporal.dayOfWeek.value
                    val daysDiff = targetDow - calDow
                    temporal.minusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                }
                is LocalDateTime -> {
                    val calDow = temporal.dayOfWeek.value
                    val daysDiff = targetDow - calDow
                    temporal.minusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                }
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for previous: $temporal")
            }
        }

        actual fun previousOrSame(dayOfWeek: DayOfWeek): TemporalAdjuster = TemporalAdjuster { temporal ->
            val targetDow = dayOfWeek.value
            when (temporal) {
                is LocalDate -> {
                    val calDow = temporal.dayOfWeek.value
                    if (calDow == targetDow) temporal else {
                        val daysDiff = targetDow - calDow
                        temporal.minusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                    }
                }
                is LocalDateTime -> {
                    val calDow = temporal.dayOfWeek.value
                    if (calDow == targetDow) temporal else {
                        val daysDiff = targetDow - calDow
                        temporal.minusDays(if (daysDiff >= 0) (7 - daysDiff).toLong() else (-daysDiff).toLong())
                    }
                }
                else -> throw UnsupportedTemporalTypeException("Unsupported temporal for previousOrSame: $temporal")
            }
        }
    }
}
