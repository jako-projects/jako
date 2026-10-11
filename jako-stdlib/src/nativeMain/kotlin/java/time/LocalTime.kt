package java.time

import java.io.Serializable
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

actual class LocalTime private constructor(
    actual val hour: Int,
    actual val minute: Int,
    actual val second: Int,
    actual val nano: Int
) : TemporalAccessor, Serializable, Comparable<LocalTime> {

    actual fun getHour(): Int = hour
    actual fun getMinute(): Int = minute
    actual fun getSecond(): Int = second
    actual fun getNano(): Int = nano

    actual fun toSecondOfDay(): Int = hour * 3600 + minute * 60 + second

    actual fun toNanoOfDay(): Long = hour * NANOS_PER_HOUR + minute * NANOS_PER_MINUTE + second * NANOS_PER_SECOND + nano

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

    actual fun minusHours(hoursToSubtract: Long): LocalTime = plusHours(-(hoursToSubtract % HOURS_PER_DAY))

    actual fun minusMinutes(minutesToSubtract: Long): LocalTime = plusMinutes(-(minutesToSubtract % MINUTES_PER_DAY))

    actual fun minusSeconds(secondsToSubtract: Long): LocalTime = plusSeconds(-(secondsToSubtract % SECONDS_PER_DAY))

    actual fun minusNanos(nanosToSubtract: Long): LocalTime = plusNanos(-(nanosToSubtract % NANOS_PER_DAY))

    actual fun format(formatter: DateTimeFormatter): String = formatter.format(this)

    actual fun isAfter(other: LocalTime): Boolean = compareTo(other) > 0
    actual fun isBefore(other: LocalTime): Boolean = compareTo(other) < 0

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

        actual fun now(): LocalTime = now(ZoneId.systemDefault())

        actual fun now(zone: ZoneId): LocalTime {
            val instant = Instant.now()
            val offset = zone.offsetAt(instant)
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
