package java.time

import java.io.Serializable
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalUnit
import java.time.temporal.UnsupportedTemporalTypeException

actual class OffsetTime private constructor(
    private val time: LocalTime,
    actual val offset: ZoneOffset
) : Temporal, TemporalAdjuster, Comparable<OffsetTime>, Serializable {

    actual val hour: Int get() = time.hour
    actual val minute: Int get() = time.minute
    actual val second: Int get() = time.second
    actual val nano: Int get() = time.nano

    actual fun getHour(): Int = time.hour
    actual fun getMinute(): Int = time.minute
    actual fun getSecond(): Int = time.second
    actual fun getNano(): Int = time.nano
    actual fun getOffset(): ZoneOffset = offset
    actual fun toLocalTime(): LocalTime = time

    actual fun withOffsetSameLocal(offset: ZoneOffset): OffsetTime =
        if (offset == this.offset) this else OffsetTime(time, offset)

    actual fun withOffsetSameInstant(offset: ZoneOffset): OffsetTime {
        if (offset == this.offset) return this
        val diffSecs = offset.totalSeconds - this.offset.totalSeconds
        return OffsetTime(time.plusSeconds(diffSecs.toLong()), offset)
    }

    actual fun plus(amountToAdd: Long, unit: TemporalUnit): OffsetTime {
        if (unit is ChronoUnit) {
            return OffsetTime(time.plus(amountToAdd, unit), offset)
        }
        return unit.addTo(this, amountToAdd)
    }

    actual fun plusHours(hours: Long): OffsetTime = OffsetTime(time.plusHours(hours), offset)
    actual fun plusMinutes(minutes: Long): OffsetTime = OffsetTime(time.plusMinutes(minutes), offset)
    actual fun plusSeconds(seconds: Long): OffsetTime = OffsetTime(time.plusSeconds(seconds), offset)
    actual fun plusNanos(nanos: Long): OffsetTime = OffsetTime(time.plusNanos(nanos), offset)

    actual fun minus(amountToSubtract: Long, unit: TemporalUnit): OffsetTime {
        return if (amountToSubtract == Long.MIN_VALUE) {
            plus(Long.MAX_VALUE, unit).plus(1, unit)
        } else {
            plus(-amountToSubtract, unit)
        }
    }

    actual fun minusHours(hours: Long): OffsetTime = OffsetTime(time.minusHours(hours), offset)
    actual fun minusMinutes(minutes: Long): OffsetTime = OffsetTime(time.minusMinutes(minutes), offset)
    actual fun minusSeconds(seconds: Long): OffsetTime = OffsetTime(time.minusSeconds(seconds), offset)
    actual fun minusNanos(nanos: Long): OffsetTime = OffsetTime(time.minusNanos(nanos), offset)

    actual fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        val end = from(endExclusive)
        if (unit is ChronoUnit) {
            val nanosUntil = end.toEpochNano() - toEpochNano()
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

    actual fun atDate(date: LocalDate): OffsetDateTime = OffsetDateTime.of(date, time, offset)

    private fun toEpochNano(): Long = time.toNanoOfDay() - offset.totalSeconds * NANOS_PER_SECOND

    actual fun isAfter(other: OffsetTime): Boolean = toEpochNano() > other.toEpochNano()
    actual fun isBefore(other: OffsetTime): Boolean = toEpochNano() < other.toEpochNano()
    actual fun isEqual(other: OffsetTime): Boolean = toEpochNano() == other.toEpochNano()

    actual override fun adjustInto(temporal: Temporal): Temporal {
        return when (temporal) {
            is OffsetTime -> this
            is OffsetDateTime -> OffsetDateTime.of(temporal.toLocalDate(), time, offset)
            else -> throw DateTimeException("Unable to adjust temporal with OffsetTime: $temporal")
        }
    }

    actual override fun compareTo(other: OffsetTime): Int {
        if (offset == other.offset) {
            return time.compareTo(other.time)
        }
        val cmp = toEpochNano().compareTo(other.toEpochNano())
        if (cmp != 0) return cmp
        return time.compareTo(other.time)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OffsetTime) return false
        return time == other.time && offset == other.offset
    }

    override fun hashCode(): Int = time.hashCode() xor offset.hashCode()

    override fun toString(): String = time.toString() + offset.toString()

    actual companion object {
        actual val MIN: OffsetTime = OffsetTime(LocalTime.MIN, ZoneOffset.MAX)
        actual val MAX: OffsetTime = OffsetTime(LocalTime.MAX, ZoneOffset.MIN)

        actual fun now(): OffsetTime = now(Clock.systemDefaultZone())
        actual fun now(zone: ZoneId): OffsetTime = now(Clock.system(zone))
        actual fun now(clock: Clock): OffsetTime = ofInstant(clock.instant(), clock.zone)

        actual fun of(time: LocalTime, offset: ZoneOffset): OffsetTime = OffsetTime(time, offset)

        actual fun of(hour: Int, minute: Int, second: Int, nanoOfSecond: Int, offset: ZoneOffset): OffsetTime =
            OffsetTime(LocalTime.of(hour, minute, second, nanoOfSecond), offset)

        actual fun ofInstant(instant: Instant, zone: ZoneId): OffsetTime {
            val offset = zone.offsetAt(instant)
            var secsOfDay = (instant.epochSecond + offset.totalSeconds).mod(SECONDS_PER_DAY)
            if (secsOfDay < 0) secsOfDay += SECONDS_PER_DAY
            val time = LocalTime.ofNanoOfDay(secsOfDay * NANOS_PER_SECOND + instant.nano)
            return OffsetTime(time, offset)
        }

        actual fun parse(text: CharSequence): OffsetTime {
            val s = text.toString()
            val offsetStart = findOffsetStart(s)
                ?: throw DateTimeParseException("Text '$s' could not be parsed as OffsetTime", s, 0)
            val time = parseIsoLocalTimeString(s.substring(0, offsetStart))
            val offsetStr = s.substring(offsetStart)
            val offset = try {
                if (offsetStr == "Z") ZoneOffset.UTC else ZoneOffset.of(offsetStr)
            } catch (ex: DateTimeException) {
                throw DateTimeParseException("Text '$s' could not be parsed as OffsetTime", s, offsetStart)
            }
            return OffsetTime(time, offset)
        }

        actual fun from(temporal: TemporalAccessor): OffsetTime {
            if (temporal is OffsetTime) return temporal
            if (temporal is OffsetDateTime) return OffsetTime(temporal.toLocalTime(), temporal.offset)
            if (temporal is ZonedDateTime) return OffsetTime(temporal.toLocalTime(), temporal.offset)
            throw DateTimeException("Unable to obtain OffsetTime from TemporalAccessor: $temporal")
        }
    }
}
