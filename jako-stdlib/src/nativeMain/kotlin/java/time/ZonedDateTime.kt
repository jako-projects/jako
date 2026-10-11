package java.time

import java.io.Serializable
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAccessor

actual class ZonedDateTime private constructor(
    private val dateTime: LocalDateTime,
    actual val offset: ZoneOffset,
    actual val zone: ZoneId
) : TemporalAccessor, Serializable {

    actual fun getZone(): ZoneId = zone
    actual fun getOffset(): ZoneOffset = offset
    actual fun toLocalDateTime(): LocalDateTime = dateTime
    actual fun toLocalDate(): LocalDate = dateTime.toLocalDate()
    actual fun toLocalTime(): LocalTime = dateTime.toLocalTime()
    actual fun toInstant(): Instant = Instant.ofEpochSecond(toEpochSecond(), dateTime.nano.toLong())
    actual fun toOffsetDateTime(): OffsetDateTime = OffsetDateTime.of(dateTime, offset)
    actual fun toEpochSecond(): Long = dateTime.toEpochSecond(offset)

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

        actual fun of(localDateTime: LocalDateTime, zone: ZoneId): ZonedDateTime {
            val offset = zone.offsetAt(localDateTime)
            return ZonedDateTime(localDateTime, offset, zone)
        }

        actual fun of(date: LocalDate, time: LocalTime, zone: ZoneId): ZonedDateTime =
            of(LocalDateTime.of(date, time), zone)

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
            return ZonedDateTime(odt.toLocalDateTime(), odt.offset, zone)
        }

        actual fun from(temporal: TemporalAccessor): ZonedDateTime {
            if (temporal is ZonedDateTime) return temporal
            throw DateTimeException("Unable to obtain ZonedDateTime from TemporalAccessor: $temporal")
        }
    }
}
