package java.time

import java.io.Serializable
import java.time.format.DateTimeParseException
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster

actual class OffsetDateTime private constructor(
    private val dateTime: LocalDateTime,
    actual val offset: ZoneOffset
) : Temporal, TemporalAdjuster, Serializable, Comparable<OffsetDateTime> {

    actual fun getOffset(): ZoneOffset = offset
    actual fun toLocalDateTime(): LocalDateTime = dateTime
    actual fun toLocalDate(): LocalDate = dateTime.toLocalDate()
    actual fun toLocalTime(): LocalTime = dateTime.toLocalTime()
    actual fun toInstant(): Instant = Instant.ofEpochSecond(toEpochSecond(), dateTime.nano.toLong())
    actual fun toEpochSecond(): Long = dateTime.toEpochSecond(offset)

    actual fun isAfter(other: OffsetDateTime): Boolean {
        val secsThis = toEpochSecond()
        val secsOth = other.toEpochSecond()
        return secsThis > secsOth || (secsThis == secsOth && dateTime.nano > other.dateTime.nano)
    }

    actual fun isBefore(other: OffsetDateTime): Boolean {
        val secsThis = toEpochSecond()
        val secsOth = other.toEpochSecond()
        return secsThis < secsOth || (secsThis == secsOth && dateTime.nano < other.dateTime.nano)
    }

    actual override fun adjustInto(temporal: Temporal): Temporal {
        return when (temporal) {
            is OffsetDateTime -> this
            is ZonedDateTime -> ZonedDateTime.of(dateTime, temporal.zone)
            else -> throw DateTimeException("Unable to adjust temporal with OffsetDateTime: $temporal")
        }
    }

    actual override fun compareTo(other: OffsetDateTime): Int {
        var cmp = toEpochSecond().compareTo(other.toEpochSecond())
        if (cmp == 0) {
            cmp = dateTime.nano.compareTo(other.dateTime.nano)
            if (cmp == 0) {
                cmp = dateTime.compareTo(other.dateTime)
            }
        }
        return cmp
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OffsetDateTime) return false
        return dateTime == other.dateTime && offset == other.offset
    }

    override fun hashCode(): Int = dateTime.hashCode() xor offset.hashCode()

    override fun toString(): String = dateTime.toString() + offset.toString()

    actual companion object {
        actual val MIN: OffsetDateTime = OffsetDateTime(LocalDateTime.MIN, ZoneOffset.MAX)
        actual val MAX: OffsetDateTime = OffsetDateTime(LocalDateTime.MAX, ZoneOffset.MIN)

        actual fun now(): OffsetDateTime = now(ZoneId.systemDefault())

        actual fun now(zone: ZoneId): OffsetDateTime {
            val instant = Instant.now()
            return ofInstant(instant, zone)
        }

        actual fun of(dateTime: LocalDateTime, offset: ZoneOffset): OffsetDateTime =
            OffsetDateTime(dateTime, offset)

        actual fun of(date: LocalDate, time: LocalTime, offset: ZoneOffset): OffsetDateTime =
            OffsetDateTime(LocalDateTime.of(date, time), offset)

        actual fun ofInstant(instant: Instant, zone: ZoneId): OffsetDateTime {
            val offset = zone.offsetAt(instant)
            val ldt = LocalDateTime.ofEpochSecond(instant.epochSecond, instant.nano, offset)
            return OffsetDateTime(ldt, offset)
        }

        actual fun parse(text: CharSequence): OffsetDateTime {
            return parseOffsetDateTime(text.toString())
        }

        actual fun from(temporal: TemporalAccessor): OffsetDateTime {
            if (temporal is OffsetDateTime) return temporal
            if (temporal is ZonedDateTime) return temporal.toOffsetDateTime()
            throw DateTimeException("Unable to obtain OffsetDateTime from TemporalAccessor: $temporal")
        }
    }
}

internal fun parseOffsetDateTime(text: String): OffsetDateTime {
    val t = text.indexOf('T')
    if (t < 0) {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed", text, 0)
    }
    val rest = text.substring(t + 1)
    val offsetStart = findOffsetStart(rest)
        ?: throw java.time.format.DateTimeParseException("Text '$text' could not be parsed", text, t + 1)
    val date = try {
        parseIsoLocalDateString(text.substring(0, t))
    } catch (ex: DateTimeParseException) {
        throw DateTimeParseException("Text '$text' could not be parsed", text, 0)
    } catch (ex: DateTimeException) {
        throw DateTimeParseException("Text '$text' could not be parsed", text, 0)
    }
    val time = try {
        parseIsoLocalTimeString(rest.substring(0, offsetStart))
    } catch (ex: RuntimeException) {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed", text, t + 1)
    }
    val offsetId = rest.substring(offsetStart)
    val offset = try {
        if (offsetId == "Z") ZoneOffset.UTC else ZoneOffset.of(offsetId)
    } catch (ex: DateTimeException) {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed", text, t + 1 + offsetStart)
    }
    return OffsetDateTime.of(date, time, offset)
}

internal fun findOffsetStart(timeAndOffset: String): Int? {
    val z = timeAndOffset.indexOf('Z')
    if (z >= 0) return z
    var plus = timeAndOffset.lastIndexOf('+')
    var minus = timeAndOffset.lastIndexOf('-')
    // time uses ':' but not '+' / '-' except in offset
    val idx = maxOf(plus, minus)
    return if (idx > 0) idx else null
}

internal fun parseIsoLocalDateString(text: String): LocalDate {
    if (text.length < 10) {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed", text, 0)
    }
    val len = text.length
    if (text[len - 3] != '-' || text[len - 6] != '-') {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed", text, 0)
    }
    val day = parseExactInt(text, len - 2, len, "day")
    val month = parseExactInt(text, len - 5, len - 3, "month")
    val yearStr = text.substring(0, len - 6)
    val year = yearStr.toIntOrNull()
        ?: throw java.time.format.DateTimeParseException("Text '$text' could not be parsed", text, 0)
    return try {
        LocalDate.of(year, month, day)
    } catch (ex: DateTimeException) {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed: ${ex.message}", text, 0)
    }
}

internal fun parseIsoLocalTimeString(text: String): LocalTime {
    if (text.length < 5) {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed", text, 0)
    }
    val hour = parseExactInt(text, 0, 2, "hour")
    if (text[2] != ':') {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed", text, 2)
    }
    val minute = parseExactInt(text, 3, 5, "minute")
    if (text.length == 5) {
        return LocalTime.of(hour, minute)
    }
    if (text[5] != ':') {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed", text, 5)
    }
    if (text.length < 8) {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed", text, 6)
    }
    val second = parseExactInt(text, 6, 8, "second")
    if (text.length == 8) {
        return LocalTime.of(hour, minute, second)
    }
    if (text[8] != '.') {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed", text, 8)
    }
    val frac = text.substring(9)
    if (frac.isEmpty() || frac.length > 9 || frac.any { it !in '0'..'9' }) {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed", text, 9)
    }
    val nano = frac.padEnd(9, '0').toInt()
    return try {
        LocalTime.of(hour, minute, second, nano)
    } catch (ex: DateTimeException) {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed: ${ex.message}", text, 0)
    }
}

internal fun parseIsoLocalDateTimeString(text: String): LocalDateTime {
    val t = text.indexOf('T')
    if (t < 0) {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed", text, 0)
    }
    val date = parseIsoLocalDateString(text.substring(0, t))
    val time = parseIsoLocalTimeString(text.substring(t + 1))
    return LocalDateTime.of(date, time)
}

private fun parseExactInt(text: String, start: Int, end: Int, field: String): Int {
    if (end > text.length) {
        throw java.time.format.DateTimeParseException("Text '$text' could not be parsed at $field", text, start)
    }
    val slice = text.substring(start, end)
    return slice.toIntOrNull()
        ?: throw java.time.format.DateTimeParseException("Text '$text' could not be parsed at $field", text, start)
}
