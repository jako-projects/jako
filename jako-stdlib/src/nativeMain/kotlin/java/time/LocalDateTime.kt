package java.time

import java.io.Serializable
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

actual class LocalDateTime private constructor(
    private val date: LocalDate,
    private val time: LocalTime
) : TemporalAccessor, Serializable, Comparable<LocalDateTime> {

    actual val year: Int get() = date.year
    actual val month: Month get() = date.month
    actual val monthValue: Int get() = date.monthValue
    actual val dayOfMonth: Int get() = date.dayOfMonth
    actual val hour: Int get() = time.hour
    actual val minute: Int get() = time.minute
    actual val second: Int get() = time.second
    actual val nano: Int get() = time.nano

    actual fun getYear(): Int = year
    actual fun getMonth(): Month = month
    actual fun getMonthValue(): Int = monthValue
    actual fun getDayOfMonth(): Int = dayOfMonth
    actual fun getHour(): Int = hour
    actual fun getMinute(): Int = minute
    actual fun getSecond(): Int = second
    actual fun getNano(): Int = nano

    actual fun toLocalDate(): LocalDate = date
    actual fun toLocalTime(): LocalTime = time

    actual fun plusYears(years: Long): LocalDateTime = with(date.plusYears(years), time)
    actual fun plusMonths(months: Long): LocalDateTime = with(date.plusMonths(months), time)
    actual fun plusWeeks(weeks: Long): LocalDateTime = with(date.plusWeeks(weeks), time)
    actual fun plusDays(days: Long): LocalDateTime = with(date.plusDays(days), time)
    actual fun plusHours(hours: Long): LocalDateTime = plusWithOverflow(date, hours, 0, 0, 0, 1)
    actual fun plusMinutes(minutes: Long): LocalDateTime = plusWithOverflow(date, 0, minutes, 0, 0, 1)
    actual fun plusSeconds(seconds: Long): LocalDateTime = plusWithOverflow(date, 0, 0, seconds, 0, 1)
    actual fun plusNanos(nanos: Long): LocalDateTime = plusWithOverflow(date, 0, 0, 0, nanos, 1)

    actual fun minusYears(years: Long): LocalDateTime =
        if (years == Long.MIN_VALUE) plusYears(Long.MAX_VALUE).plusYears(1) else plusYears(-years)

    actual fun minusMonths(months: Long): LocalDateTime =
        if (months == Long.MIN_VALUE) plusMonths(Long.MAX_VALUE).plusMonths(1) else plusMonths(-months)

    actual fun minusWeeks(weeks: Long): LocalDateTime =
        if (weeks == Long.MIN_VALUE) plusWeeks(Long.MAX_VALUE).plusWeeks(1) else plusWeeks(-weeks)

    actual fun minusDays(days: Long): LocalDateTime =
        if (days == Long.MIN_VALUE) plusDays(Long.MAX_VALUE).plusDays(1) else plusDays(-days)

    actual fun minusHours(hours: Long): LocalDateTime = plusWithOverflow(date, hours, 0, 0, 0, -1)
    actual fun minusMinutes(minutes: Long): LocalDateTime = plusWithOverflow(date, 0, minutes, 0, 0, -1)
    actual fun minusSeconds(seconds: Long): LocalDateTime = plusWithOverflow(date, 0, 0, seconds, 0, -1)
    actual fun minusNanos(nanos: Long): LocalDateTime = plusWithOverflow(date, 0, 0, 0, nanos, -1)

    actual fun atZone(zone: ZoneId): ZonedDateTime = ZonedDateTime.of(this, zone)
    actual fun atOffset(offset: ZoneOffset): OffsetDateTime = OffsetDateTime.of(this, offset)

    actual fun toEpochSecond(offset: ZoneOffset): Long {
        val epochDay = date.toEpochDay()
        val secs = epochDay * SECONDS_PER_DAY + time.toSecondOfDay()
        return secs - offset.totalSeconds
    }

    actual fun format(formatter: DateTimeFormatter): String = formatter.format(this)

    actual fun isAfter(other: LocalDateTime): Boolean = compareTo(other) > 0
    actual fun isBefore(other: LocalDateTime): Boolean = compareTo(other) < 0

    actual override fun compareTo(other: LocalDateTime): Int {
        val cmp = date.compareTo(other.date)
        return if (cmp != 0) cmp else time.compareTo(other.time)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocalDateTime) return false
        return date == other.date && time == other.time
    }

    override fun hashCode(): Int = date.hashCode() xor time.hashCode()

    override fun toString(): String = date.toString() + "T" + time.toString()

    private fun plusWithOverflow(
        newDate: LocalDate,
        hours: Long,
        minutes: Long,
        seconds: Long,
        nanos: Long,
        sign: Int
    ): LocalDateTime {
        if ((hours or minutes or seconds or nanos) == 0L) {
            return with(newDate, time)
        }
        var totDays = nanos / NANOS_PER_DAY + seconds / SECONDS_PER_DAY +
            minutes / MINUTES_PER_DAY + hours / HOURS_PER_DAY
        totDays *= sign
        var totNanos = nanos % NANOS_PER_DAY +
            (seconds % SECONDS_PER_DAY) * NANOS_PER_SECOND +
            (minutes % MINUTES_PER_DAY) * NANOS_PER_MINUTE +
            (hours % HOURS_PER_DAY) * NANOS_PER_HOUR
        val curNoD = time.toNanoOfDay()
        totNanos = totNanos * sign + curNoD
        totDays += totNanos.floorDiv(NANOS_PER_DAY)
        val newNoD = totNanos.mod(NANOS_PER_DAY)
        val newTime = if (newNoD == curNoD) time else LocalTime.ofNanoOfDay(newNoD)
        return with(newDate.plusDays(totDays), newTime)
    }

    private fun with(newDate: LocalDate, newTime: LocalTime): LocalDateTime {
        if (date == newDate && time == newTime) return this
        return LocalDateTime(newDate, newTime)
    }

    actual companion object {
        actual val MIN: LocalDateTime = LocalDateTime(LocalDate.MIN, LocalTime.MIN)
        actual val MAX: LocalDateTime = LocalDateTime(LocalDate.MAX, LocalTime.MAX)

        actual fun now(): LocalDateTime = now(ZoneId.systemDefault())

        actual fun now(zone: ZoneId): LocalDateTime {
            val instant = Instant.now()
            return ofInstant(instant, zone)
        }

        actual fun of(date: LocalDate, time: LocalTime): LocalDateTime = LocalDateTime(date, time)

        actual fun of(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int): LocalDateTime =
            LocalDateTime(LocalDate.of(year, month, dayOfMonth), LocalTime.of(hour, minute))

        actual fun of(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int, second: Int): LocalDateTime =
            LocalDateTime(LocalDate.of(year, month, dayOfMonth), LocalTime.of(hour, minute, second))

        actual fun of(
            year: Int,
            month: Int,
            dayOfMonth: Int,
            hour: Int,
            minute: Int,
            second: Int,
            nanoOfSecond: Int
        ): LocalDateTime =
            LocalDateTime(LocalDate.of(year, month, dayOfMonth), LocalTime.of(hour, minute, second, nanoOfSecond))

        actual fun ofEpochSecond(epochSecond: Long, nanoOfSecond: Int, offset: ZoneOffset): LocalDateTime {
            if (nanoOfSecond < 0 || nanoOfSecond > 999_999_999) {
                throw DateTimeException("Invalid value for NanoOfSecond: $nanoOfSecond")
            }
            val localSecond = epochSecond + offset.totalSeconds
            val localEpochDay = localSecond.floorDiv(SECONDS_PER_DAY)
            val secsOfDay = localSecond.mod(SECONDS_PER_DAY).toInt()
            return of(LocalDate.ofEpochDay(localEpochDay), LocalTime.ofNanoOfDay(secsOfDay * NANOS_PER_SECOND + nanoOfSecond))
        }

        actual fun ofInstant(instant: Instant, zone: ZoneId): LocalDateTime {
            val offset = zone.offsetAt(instant)
            return ofEpochSecond(instant.epochSecond, instant.nano, offset)
        }

        actual fun parse(text: CharSequence): LocalDateTime = parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        actual fun parse(text: CharSequence, formatter: DateTimeFormatter): LocalDateTime {
            return from(formatter.parse(text))
        }

        actual fun from(temporal: TemporalAccessor): LocalDateTime {
            if (temporal is LocalDateTime) return temporal
            if (temporal is OffsetDateTime) return temporal.toLocalDateTime()
            if (temporal is ZonedDateTime) return temporal.toLocalDateTime()
            if (temporal is LocalDate) {
                throw DateTimeException("Unable to obtain LocalDateTime from TemporalAccessor: $temporal")
            }
            throw DateTimeException("Unable to obtain LocalDateTime from TemporalAccessor: $temporal")
        }
    }
}
