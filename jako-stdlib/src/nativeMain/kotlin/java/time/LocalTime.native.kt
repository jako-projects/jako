package java.time

import java.io.Serializable
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAmount
import java.time.temporal.TemporalField
import java.time.temporal.TemporalUnit
import java.time.temporal.UnsupportedTemporalTypeException
import java.time.temporal.ValueRange

actual class LocalTime private constructor(
    actual val hour: Int,
    actual val minute: Int,
    actual val second: Int,
    actual val nano: Int
) : Temporal, TemporalAdjuster, Serializable, Comparable<LocalTime> {

    actual fun isSupported(field: TemporalField?): Boolean {
        if (field is ChronoField) return field.isTimeBased
        return field != null && field.isSupportedBy(this)
    }

    actual fun isSupported(unit: TemporalUnit?): Boolean {
        if (unit is ChronoUnit) return unit.isTimeBased
        return unit != null && unit.isSupportedBy(this)
    }

    actual fun range(field: TemporalField): ValueRange = field.rangeRefinedBy(this)

    actual fun get(field: TemporalField): Int {
        return range(field).checkValidIntValue(getLong(field), field)
    }

    actual fun getLong(field: TemporalField): Long = field.getFrom(this)

    actual fun getHour(): Int = hour
    actual fun getMinute(): Int = minute
    actual fun getSecond(): Int = second
    actual fun getNano(): Int = nano

    actual fun toSecondOfDay(): Int = hour * 3600 + minute * 60 + second

    actual fun toNanoOfDay(): Long = hour * NANOS_PER_HOUR + minute * NANOS_PER_MINUTE + second * NANOS_PER_SECOND + nano

    actual fun with(adjuster: TemporalAdjuster): LocalTime {
        if (adjuster is LocalTime) return adjuster
        return adjuster.adjustInto(this) as LocalTime
    }

    actual fun with(field: TemporalField, newValue: Long): LocalTime {
        if (field is ChronoField) {
            field.checkValidValue(newValue)
            return when (field) {
                ChronoField.NANO_OF_SECOND -> withNano(newValue.toInt())
                ChronoField.NANO_OF_DAY -> ofNanoOfDay(newValue)
                ChronoField.MICRO_OF_SECOND -> withNano(newValue.toInt() * 1000)
                ChronoField.MICRO_OF_DAY -> ofNanoOfDay(newValue * 1000L)
                ChronoField.MILLI_OF_SECOND -> withNano(newValue.toInt() * 1_000_000)
                ChronoField.MILLI_OF_DAY -> ofNanoOfDay(newValue * 1_000_000L)
                ChronoField.SECOND_OF_MINUTE -> withSecond(newValue.toInt())
                ChronoField.SECOND_OF_DAY -> plusSeconds(newValue - toSecondOfDay())
                ChronoField.MINUTE_OF_HOUR -> withMinute(newValue.toInt())
                ChronoField.MINUTE_OF_DAY -> plusMinutes(newValue - (hour * 60 + minute))
                ChronoField.HOUR_OF_AMPM -> plusHours(newValue - (hour % 12))
                ChronoField.CLOCK_HOUR_OF_AMPM -> plusHours((if (newValue == 12L) 0L else newValue) - (hour % 12))
                ChronoField.HOUR_OF_DAY -> withHour(newValue.toInt())
                ChronoField.CLOCK_HOUR_OF_DAY -> withHour(if (newValue == 24L) 0 else newValue.toInt())
                ChronoField.AMPM_OF_DAY -> plusHours((newValue - (hour / 12)) * 12)
                else -> throw UnsupportedTemporalTypeException("Unsupported field: $field")
            }
        }
        throw UnsupportedTemporalTypeException("Unsupported field: $field")
    }

    actual fun withHour(hour: Int): LocalTime {
        if (this.hour == hour) return this
        return create(hour, minute, second, nano)
    }

    actual fun withMinute(minute: Int): LocalTime {
        if (this.minute == minute) return this
        return create(hour, minute, second, nano)
    }

    actual fun withSecond(second: Int): LocalTime {
        if (this.second == second) return this
        return create(hour, minute, second, nano)
    }

    actual fun withNano(nanoOfSecond: Int): LocalTime {
        if (this.nano == nanoOfSecond) return this
        return create(hour, minute, second, nanoOfSecond)
    }

    actual fun truncatedTo(unit: TemporalUnit): LocalTime {
        if (unit == ChronoUnit.NANOS) return this
        val unitDur = unit.duration
        if (unitDur.seconds > SECONDS_PER_DAY) {
            throw UnsupportedTemporalTypeException("Unit is too large to be used for truncation")
        }
        val dur = unitDur.toNanos()
        if ((NANOS_PER_DAY % dur) != 0L) {
            throw UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder")
        }
        val nod = toNanoOfDay()
        return ofNanoOfDay((nod / dur) * dur)
    }

    actual fun plus(amountToAdd: TemporalAmount): LocalTime {
        if (amountToAdd is Duration) {
            return plusNanos(amountToAdd.toNanos())
        }
        throw DateTimeException("Unsupported TemporalAmount for LocalTime: $amountToAdd")
    }

    actual fun plus(amountToAdd: Long, unit: TemporalUnit): LocalTime {
        if (unit is ChronoUnit) {
            return when (unit) {
                ChronoUnit.NANOS -> plusNanos(amountToAdd)
                ChronoUnit.MICROS -> plusNanos((amountToAdd % 86_400_000_000L) * 1000L)
                ChronoUnit.MILLIS -> plusNanos((amountToAdd % 86_400_000L) * 1_000_000L)
                ChronoUnit.SECONDS -> plusSeconds(amountToAdd)
                ChronoUnit.MINUTES -> plusMinutes(amountToAdd)
                ChronoUnit.HOURS -> plusHours(amountToAdd)
                ChronoUnit.HALF_DAYS -> plusHours((amountToAdd % 2L) * 12L)
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.addTo(this, amountToAdd)
    }

    actual fun plusHours(hoursToAdd: Long): LocalTime {
        if (hoursToAdd == 0L) return this
        val newHour = ((hoursToAdd % HOURS_PER_DAY).toInt() + hour + 24) % 24
        return create(newHour, minute, second, nano)
    }

    actual fun plusMinutes(minutesToAdd: Long): LocalTime {
        if (minutesToAdd == 0L) return this
        val mofd = hour * 60 + minute
        val newMofd = ((minutesToAdd % MINUTES_PER_DAY).toInt() + mofd + 1440) % 1440
        if (mofd == newMofd) return this
        return create(newMofd / 60, newMofd % 60, second, nano)
    }

    actual fun plusSeconds(secondstoAdd: Long): LocalTime {
        if (secondstoAdd == 0L) return this
        val sofd = toSecondOfDay()
        val newSofd = ((secondstoAdd % SECONDS_PER_DAY).toInt() + sofd + 86400) % 86400
        if (sofd == newSofd) return this
        return create(newSofd / 3600, (newSofd / 60) % 60, newSofd % 60, nano)
    }

    actual fun plusNanos(nanosToAdd: Long): LocalTime {
        if (nanosToAdd == 0L) return this
        val nofd = toNanoOfDay()
        val newNofd = ((nanosToAdd % NANOS_PER_DAY) + nofd + NANOS_PER_DAY) % NANOS_PER_DAY
        if (nofd == newNofd) return this
        return ofNanoOfDay(newNofd)
    }

    actual fun minus(amountToSubtract: TemporalAmount): LocalTime {
        if (amountToSubtract is Duration) {
            return minusNanos(amountToSubtract.toNanos())
        }
        throw DateTimeException("Unsupported TemporalAmount for LocalTime: $amountToSubtract")
    }

    actual fun minus(amountToSubtract: Long, unit: TemporalUnit): LocalTime {
        return if (amountToSubtract == Long.MIN_VALUE) {
            plus(Long.MAX_VALUE, unit).plus(1, unit)
        } else {
            plus(-amountToSubtract, unit)
        }
    }

    actual fun minusHours(hoursToSubtract: Long): LocalTime = plusHours(-(hoursToSubtract % HOURS_PER_DAY))

    actual fun minusMinutes(minutesToSubtract: Long): LocalTime = plusMinutes(-(minutesToSubtract % MINUTES_PER_DAY))

    actual fun minusSeconds(secondsToSubtract: Long): LocalTime = plusSeconds(-(secondsToSubtract % SECONDS_PER_DAY))

    actual fun minusNanos(nanosToSubtract: Long): LocalTime = plusNanos(-(nanosToSubtract % NANOS_PER_DAY))

    actual fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        val end = from(endExclusive)
        if (unit is ChronoUnit) {
            val nanosUntil = end.toNanoOfDay() - toNanoOfDay()
            return when (unit) {
                ChronoUnit.NANOS -> nanosUntil
                ChronoUnit.MICROS -> nanosUntil / 1000L
                ChronoUnit.MILLIS -> nanosUntil / 1_000_000L
                ChronoUnit.SECONDS -> nanosUntil / NANOS_PER_SECOND
                ChronoUnit.MINUTES -> nanosUntil / NANOS_PER_MINUTE
                ChronoUnit.HOURS -> nanosUntil / NANOS_PER_HOUR
                ChronoUnit.HALF_DAYS -> nanosUntil / (12 * NANOS_PER_HOUR)
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.between(this, end)
    }

    actual fun atDate(date: LocalDate): LocalDateTime = LocalDateTime.of(date, this)

    actual fun atOffset(offset: ZoneOffset): OffsetTime = OffsetTime.of(this, offset)

    actual fun format(formatter: DateTimeFormatter): String = formatter.format(this)

    actual fun isAfter(other: LocalTime): Boolean = compareTo(other) > 0
    actual fun isBefore(other: LocalTime): Boolean = compareTo(other) < 0

    actual override fun adjustInto(temporal: Temporal): Temporal {
        return when (temporal) {
            is LocalTime -> this
            is LocalDateTime -> LocalDateTime.of(temporal.toLocalDate(), this)
            is OffsetDateTime -> OffsetDateTime.of(temporal.toLocalDate(), this, temporal.offset)
            is ZonedDateTime -> ZonedDateTime.of(temporal.toLocalDate(), this, temporal.zone)
            is OffsetTime -> OffsetTime.of(this, temporal.offset)
            else -> throw DateTimeException("Unable to adjust temporal with LocalTime: $temporal")
        }
    }

    actual override fun compareTo(other: LocalTime): Int {
        var cmp = hour.compareTo(other.hour)
        if (cmp == 0) {
            cmp = minute.compareTo(other.minute)
            if (cmp == 0) {
                cmp = second.compareTo(other.second)
                if (cmp == 0) {
                    cmp = nano.compareTo(other.nano)
                }
            }
        }
        return cmp
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocalTime) return false
        return hour == other.hour && minute == other.minute && second == other.second && nano == other.nano
    }

    override fun hashCode(): Int {
        val nod = toNanoOfDay()
        return (nod xor (nod ushr 32)).toInt()
    }

    override fun toString(): String = formatIsoTime(hour, minute, second, nano, alwaysSeconds = false)

    actual companion object {
        actual val MIDNIGHT: LocalTime = LocalTime(0, 0, 0, 0)
        actual val NOON: LocalTime = LocalTime(12, 0, 0, 0)
        actual val MIN: LocalTime = MIDNIGHT
        actual val MAX: LocalTime = LocalTime(23, 59, 59, 999_999_999)

        actual fun now(): LocalTime = now(Clock.systemDefaultZone())

        actual fun now(zone: ZoneId): LocalTime = now(Clock.system(zone))

        actual fun now(clock: Clock): LocalTime {
            val instant = clock.instant()
            val offset = clock.zone.offsetAt(instant)
            val localSecond = instant.epochSecond + offset.totalSeconds
            val secOfDay = localSecond.mod(SECONDS_PER_DAY)
            return ofNanoOfDay(secOfDay * NANOS_PER_SECOND + instant.nano)
        }

        actual fun of(hour: Int, minute: Int): LocalTime = create(hour, minute, 0, 0)

        actual fun of(hour: Int, minute: Int, second: Int): LocalTime = create(hour, minute, second, 0)

        actual fun of(hour: Int, minute: Int, second: Int, nanoOfSecond: Int): LocalTime =
            create(hour, minute, second, nanoOfSecond)

        actual fun ofSecondOfDay(secondOfDay: Long): LocalTime {
            if (secondOfDay < 0 || secondOfDay > 86399) {
                throw DateTimeException("Invalid value for SecondOfDay: $secondOfDay")
            }
            val hours = (secondOfDay / 3600).toInt()
            val remainder = (secondOfDay - hours * 3600).toInt()
            return create(hours, remainder / 60, remainder % 60, 0)
        }

        actual fun ofNanoOfDay(nanoOfDay: Long): LocalTime {
            if (nanoOfDay < 0 || nanoOfDay > NANOS_PER_DAY - 1) {
                throw DateTimeException("Invalid value for NanoOfDay: $nanoOfDay")
            }
            var nod = nanoOfDay
            val hours = (nod / NANOS_PER_HOUR).toInt()
            nod -= hours * NANOS_PER_HOUR
            val minutes = (nod / NANOS_PER_MINUTE).toInt()
            nod -= minutes * NANOS_PER_MINUTE
            val seconds = (nod / NANOS_PER_SECOND).toInt()
            nod -= seconds * NANOS_PER_SECOND
            return create(hours, minutes, seconds, nod.toInt())
        }

        actual fun parse(text: CharSequence): LocalTime = parse(text, DateTimeFormatter.ISO_LOCAL_TIME)

        actual fun parse(text: CharSequence, formatter: DateTimeFormatter): LocalTime {
            // [Nullability Decision] text and formatter are non-null, matching JDK 8
            return from(formatter.parse(text))
        }

        actual fun from(temporal: TemporalAccessor): LocalTime {
            if (temporal is LocalTime) return temporal
            if (temporal is LocalDateTime) return temporal.toLocalTime()
            if (temporal is OffsetDateTime) return temporal.toLocalTime()
            if (temporal is ZonedDateTime) return temporal.toLocalTime()
            if (temporal is OffsetTime) return temporal.toLocalTime()
            throw DateTimeException("Unable to obtain LocalTime from TemporalAccessor: $temporal")
        }

        private fun create(hour: Int, minute: Int, second: Int, nanoOfSecond: Int): LocalTime {
            if (hour < 0 || hour > 23) {
                throw DateTimeException("Invalid value for HourOfDay (valid values 0 - 23): $hour")
            }
            if (minute < 0 || minute > 59) {
                throw DateTimeException("Invalid value for MinuteOfHour (valid values 0 - 59): $minute")
            }
            if (second < 0 || second > 59) {
                throw DateTimeException("Invalid value for SecondOfMinute (valid values 0 - 59): $second")
            }
            if (nanoOfSecond < 0 || nanoOfSecond > 999_999_999) {
                throw DateTimeException("Invalid value for NanoOfSecond (valid values 0 - 999999999): $nanoOfSecond")
            }
            if ((hour or minute or second or nanoOfSecond) == 0) {
                return MIDNIGHT
            }
            if (hour == 12 && (minute or second or nanoOfSecond) == 0) {
                return NOON
            }
            return LocalTime(hour, minute, second, nanoOfSecond)
        }
    }
}
