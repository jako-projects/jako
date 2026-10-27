package java.time

import java.time.temporal.ChronoField
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster

actual enum class DayOfWeek : TemporalAccessor, TemporalAdjuster {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    actual val value: Int get() = ordinal + 1
    actual fun getValue(): Int = value

    actual fun plus(days: Long): DayOfWeek {
        val amount = (days % 7).toInt()
        return values()[(ordinal + (amount + 7)) % 7]
    }

    actual fun minus(days: Long): DayOfWeek = plus(-(days % 7))

    actual override fun adjustInto(temporal: Temporal): Temporal {
        return when (temporal) {
            is LocalDate -> temporal.plusDays((this.value - temporal.dayOfWeek.value).toLong())
            is LocalDateTime -> temporal.plusDays((this.value - temporal.dayOfWeek.value).toLong())
            is OffsetDateTime -> OffsetDateTime.of(temporal.toLocalDateTime().plusDays((this.value - temporal.toLocalDate().dayOfWeek.value).toLong()), temporal.offset)
            is ZonedDateTime -> ZonedDateTime.of(temporal.toLocalDateTime().plusDays((this.value - temporal.toLocalDate().dayOfWeek.value).toLong()), temporal.zone)
            else -> throw DateTimeException("Unable to adjust temporal with DayOfWeek: $temporal")
        }
    }

    actual companion object {
        actual fun of(dayOfWeek: Int): DayOfWeek {
            if (dayOfWeek < 1 || dayOfWeek > 7) {
                throw DateTimeException("Invalid value for DayOfWeek: $dayOfWeek")
            }
            return values()[dayOfWeek - 1]
        }

        actual fun from(temporal: TemporalAccessor): DayOfWeek {
            if (temporal is DayOfWeek) return temporal
            try {
                return of(ChronoField.DAY_OF_WEEK.getFrom(temporal).toInt())
            } catch (ex: DateTimeException) {
                throw DateTimeException("Unable to obtain DayOfWeek from TemporalAccessor: $temporal", ex)
            }
        }
    }
}
