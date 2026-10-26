package java.time

import java.io.Serializable
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAmount

actual class ZonedDateTime private constructor(
    private val dateTime: LocalDateTime,
    actual val offset: ZoneOffset,
    actual val zone: ZoneId
) : Temporal, Serializable {

    actual val year: Int get() = dateTime.year
    actual val month: Month get() = dateTime.month
    actual val monthValue: Int get() = dateTime.monthValue
    actual val dayOfMonth: Int get() = dateTime.dayOfMonth
    actual val dayOfYear: Int get() = dateTime.dayOfYear
    actual val dayOfWeek: DayOfWeek get() = dateTime.dayOfWeek
    actual val hour: Int get() = dateTime.hour
    actual val minute: Int get() = dateTime.minute
    actual val second: Int get() = dateTime.second
    actual val nano: Int get() = dateTime.nano

    actual fun getZone(): ZoneId = zone
    actual fun getOffset(): ZoneOffset = offset
    actual fun getYear(): Int = dateTime.year
    actual fun getMonth(): Month = dateTime.month
    actual fun getMonthValue(): Int = dateTime.monthValue
    actual fun getDayOfMonth(): Int = dateTime.dayOfMonth
    actual fun getDayOfYear(): Int = dateTime.dayOfYear
    actual fun getDayOfWeek(): DayOfWeek = dateTime.dayOfWeek
    actual fun getHour(): Int = dateTime.hour
    actual fun getMinute(): Int = dateTime.minute
    actual fun getSecond(): Int = dateTime.second
    actual fun getNano(): Int = dateTime.nano

    actual fun withEarlierOffsetAtOverlap(): ZonedDateTime {
        val earlier = zone.earlierOffsetAtOverlap(dateTime, offset)
        return if (earlier == offset) this else ZonedDateTime(dateTime, earlier, zone)
    }

    actual fun withLaterOffsetAtOverlap(): ZonedDateTime {
        val later = zone.laterOffsetAtOverlap(dateTime, offset)
        return if (later == offset) this else ZonedDateTime(dateTime, later, zone)
    }

    actual fun withZoneSameLocal(zone: ZoneId): ZonedDateTime {
        return if (this.zone == zone) this else ofLocal(dateTime, zone, offset)
    }

    actual fun withZoneSameInstant(zone: ZoneId): ZonedDateTime {
        return if (this.zone == zone) this else ofInstant(toInstant(), zone)
    }

    actual fun plus(amountToAdd: TemporalAmount): ZonedDateTime {
        if (amountToAdd is Period) {
            return ofLocal(dateTime.plus(amountToAdd), zone, offset)
        }
        if (amountToAdd is Duration) {
            return ofInstant(toInstant().plus(amountToAdd), zone)
        }
        throw DateTimeException("Unsupported TemporalAmount for ZonedDateTime: $amountToAdd")
    }

    actual fun plusYears(years: Long): ZonedDateTime = ofLocal(dateTime.plusYears(years), zone, offset)
    actual fun plusMonths(months: Long): ZonedDateTime = ofLocal(dateTime.plusMonths(months), zone, offset)
    actual fun plusWeeks(weeks: Long): ZonedDateTime = ofLocal(dateTime.plusWeeks(weeks), zone, offset)
    actual fun plusDays(days: Long): ZonedDateTime = ofLocal(dateTime.plusDays(days), zone, offset)
    actual fun plusHours(hours: Long): ZonedDateTime = ofInstant(toInstant().plusSeconds(multiplyExact(hours, 3600L)), zone)
    actual fun plusMinutes(minutes: Long): ZonedDateTime = ofInstant(toInstant().plusSeconds(multiplyExact(minutes, 60L)), zone)
    actual fun plusSeconds(seconds: Long): ZonedDateTime = ofInstant(toInstant().plusSeconds(seconds), zone)
    actual fun plusNanos(nanos: Long): ZonedDateTime = ofInstant(toInstant().plusNanos(nanos), zone)

    actual fun minus(amountToSubtract: TemporalAmount): ZonedDateTime {
        if (amountToSubtract is Period) {
            return ofLocal(dateTime.minus(amountToSubtract), zone, offset)
        }
        if (amountToSubtract is Duration) {
            return ofInstant(toInstant().minus(amountToSubtract), zone)
        }
        throw DateTimeException("Unsupported TemporalAmount for ZonedDateTime: $amountToSubtract")
    }

    actual fun minusYears(years: Long): ZonedDateTime =
        if (years == Long.MIN_VALUE) plusYears(Long.MAX_VALUE).plusYears(1) else plusYears(-years)
    actual fun minusMonths(months: Long): ZonedDateTime =
        if (months == Long.MIN_VALUE) plusMonths(Long.MAX_VALUE).plusMonths(1) else plusMonths(-months)
    actual fun minusWeeks(weeks: Long): ZonedDateTime =
        if (weeks == Long.MIN_VALUE) plusWeeks(Long.MAX_VALUE).plusWeeks(1) else plusWeeks(-weeks)
    actual fun minusDays(days: Long): ZonedDateTime =
        if (days == Long.MIN_VALUE) plusDays(Long.MAX_VALUE).plusDays(1) else plusDays(-days)
    actual fun minusHours(hours: Long): ZonedDateTime =
        if (hours == Long.MIN_VALUE) plusHours(Long.MAX_VALUE).plusHours(1) else plusHours(-hours)
    actual fun minusMinutes(minutes: Long): ZonedDateTime =
        if (minutes == Long.MIN_VALUE) plusMinutes(Long.MAX_VALUE).plusMinutes(1) else plusMinutes(-minutes)
    actual fun minusSeconds(seconds: Long): ZonedDateTime =
        if (seconds == Long.MIN_VALUE) plusSeconds(Long.MAX_VALUE).plusSeconds(1) else plusSeconds(-seconds)
    actual fun minusNanos(nanos: Long): ZonedDateTime =
        if (nanos == Long.MIN_VALUE) plusNanos(Long.MAX_VALUE).plusNanos(1) else plusNanos(-nanos)

    actual fun toLocalDateTime(): LocalDateTime = dateTime
    actual fun toLocalDate(): LocalDate = dateTime.toLocalDate()
    actual fun toLocalTime(): LocalTime = dateTime.toLocalTime()
    actual fun toInstant(): Instant = Instant.ofEpochSecond(toEpochSecond(), dateTime.nano.toLong())
    actual fun toOffsetDateTime(): OffsetDateTime = OffsetDateTime.of(dateTime, offset)
    actual fun toEpochSecond(): Long = dateTime.toEpochSecond(offset)
    actual fun format(formatter: DateTimeFormatter): String = formatter.format(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ZonedDateTime) return false
        return dateTime == other.dateTime && offset == other.offset && zone == other.zone
    }

    override fun hashCode(): Int {
        return dateTime.hashCode() xor offset.hashCode() xor zone.hashCode().rotateLeft(3)
    }

    override fun toString(): String {
        val buf = StringBuilder(dateTime.toString()).append(offset.toString())
        if (zone !== offset && zone.id != offset.id) {
            buf.append('[').append(zone.toString()).append(']')
        }
        return buf.toString()
    }

    actual companion object {
        actual fun now(): ZonedDateTime = now(ZoneId.systemDefault())

        actual fun now(zone: ZoneId): ZonedDateTime = ofInstant(Instant.now(), zone)

        internal fun ofLocal(localDateTime: LocalDateTime, zone: ZoneId, preferredOffset: ZoneOffset?): ZonedDateTime {
            if (zone is ZoneOffset) {
                return ZonedDateTime(localDateTime, zone, zone)
            }
            val (resolvedLdt, resolvedOffset) = zone.resolveLocal(localDateTime, preferredOffset)
            return ZonedDateTime(resolvedLdt, resolvedOffset, zone)
        }

        actual fun of(localDateTime: LocalDateTime, zone: ZoneId): ZonedDateTime =
            ofLocal(localDateTime, zone, null)

        actual fun of(date: LocalDate, time: LocalTime, zone: ZoneId): ZonedDateTime =
            of(LocalDateTime.of(date, time), zone)

        actual fun of(
            year: Int,
            month: Int,
            dayOfMonth: Int,
            hour: Int,
            minute: Int,
            second: Int,
            nanoOfSecond: Int,
            zone: ZoneId
        ): ZonedDateTime = of(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond), zone)

        actual fun ofInstant(instant: Instant, zone: ZoneId): ZonedDateTime {
            val offset = zone.offsetAt(instant)
            val ldt = LocalDateTime.ofEpochSecond(instant.epochSecond, instant.nano, offset)
            return ZonedDateTime(ldt, offset, zone)
        }

        actual fun parse(text: CharSequence): ZonedDateTime {
            val s = text.toString()
            val bracket = s.indexOf('[')
            if (bracket < 0) {
                val odt = parseOffsetDateTime(s)
                return of(odt.toLocalDateTime(), odt.offset)
            }
            if (!s.endsWith(']')) {
                throw DateTimeParseException("Text '$s' could not be parsed", s, bracket)
            }
            val zone = try {
                ZoneId.of(s.substring(bracket + 1, s.length - 1))
            } catch (ex: DateTimeException) {
                throw DateTimeParseException("Text '$s' could not be parsed", s, bracket + 1)
            }
            val odt = parseOffsetDateTime(s.substring(0, bracket))
            return ofLocal(odt.toLocalDateTime(), zone, odt.offset)
        }

        actual fun parse(text: CharSequence, formatter: DateTimeFormatter): ZonedDateTime {
            return from(formatter.parse(text))
        }

        actual fun from(temporal: TemporalAccessor): ZonedDateTime {
            if (temporal is ZonedDateTime) return temporal
            if (temporal is OffsetDateTime) return of(temporal.toLocalDateTime(), temporal.offset)
            throw DateTimeException("Unable to obtain ZonedDateTime from TemporalAccessor: $temporal")
        }
    }
}

