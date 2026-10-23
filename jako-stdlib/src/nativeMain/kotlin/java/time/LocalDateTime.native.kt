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
import java.time.temporal.ValueRange

actual class LocalDateTime private constructor(
    private val date: LocalDate,
    private val time: LocalTime
) : Temporal, TemporalAdjuster, Serializable, Comparable<LocalDateTime> {

    actual val year: Int get() = date.year
    actual val month: Month get() = date.month
    actual val monthValue: Int get() = date.monthValue
    actual val dayOfMonth: Int get() = date.dayOfMonth
    actual val dayOfYear: Int get() = date.dayOfYear
    actual val dayOfWeek: DayOfWeek get() = date.dayOfWeek
    actual val hour: Int get() = time.hour
    actual val minute: Int get() = time.minute
    actual val second: Int get() = time.second
    actual val nano: Int get() = time.nano

    actual fun isSupported(field: TemporalField?): Boolean {
        if (field is ChronoField) return field.isDateBased || field.isTimeBased
        return field != null && field.isSupportedBy(this)
    }

    actual fun isSupported(unit: TemporalUnit?): Boolean {
        if (unit is ChronoUnit) return unit != ChronoUnit.FOREVER
        return unit != null && unit.isSupportedBy(this)
    }

    actual fun range(field: TemporalField): ValueRange {
        if (field is ChronoField) {
            return if (field.isTimeBased) time.range(field) else date.range(field)
        }
        return field.rangeRefinedBy(this)
    }

    actual fun get(field: TemporalField): Int {
        if (field is ChronoField) {
            return if (field.isTimeBased) time.get(field) else date.get(field)
        }
        return range(field).checkValidIntValue(getLong(field), field)
    }

    actual fun getLong(field: TemporalField): Long {
        if (field is ChronoField) {
            return if (field.isTimeBased) time.getLong(field) else date.getLong(field)
        }
        return field.getFrom(this)
    }

    actual fun getYear(): Int = year
    actual fun getMonth(): Month = month
    actual fun getMonthValue(): Int = monthValue
    actual fun getDayOfMonth(): Int = dayOfMonth
    actual fun getDayOfYear(): Int = dayOfYear
    actual fun getDayOfWeek(): DayOfWeek = dayOfWeek
    actual fun getHour(): Int = hour
    actual fun getMinute(): Int = minute
    actual fun getSecond(): Int = second
    actual fun getNano(): Int = nano

    actual fun toLocalDate(): LocalDate = date
    actual fun toLocalTime(): LocalTime = time

    actual fun with(adjuster: TemporalAdjuster): LocalDateTime {
        if (adjuster is LocalDate) return with(adjuster, time)
        if (adjuster is LocalTime) return with(date, adjuster)
        if (adjuster is LocalDateTime) return adjuster
        return adjuster.adjustInto(this) as LocalDateTime
    }

    actual fun with(field: TemporalField, newValue: Long): LocalDateTime {
        if (field is ChronoField) {
            return if (field.isTimeBased) {
                with(date, time.with(field, newValue))
            } else {
                with(date.with(field, newValue), time)
            }
        }
        throw DateTimeException("Unsupported field: $field")
    }

    actual fun withYear(year: Int): LocalDateTime = with(date.withYear(year), time)
    actual fun withMonth(month: Int): LocalDateTime = with(date.withMonth(month), time)
    actual fun withDayOfMonth(dayOfMonth: Int): LocalDateTime = with(date.withDayOfMonth(dayOfMonth), time)
    actual fun withDayOfYear(dayOfYear: Int): LocalDateTime = with(date.withDayOfYear(dayOfYear), time)
    actual fun withHour(hour: Int): LocalDateTime = with(date, time.withHour(hour))
    actual fun withMinute(minute: Int): LocalDateTime = with(date, time.withMinute(minute))
    actual fun withSecond(second: Int): LocalDateTime = with(date, time.withSecond(second))
    actual fun withNano(nanoOfSecond: Int): LocalDateTime = with(date, time.withNano(nanoOfSecond))

    actual fun truncatedTo(unit: TemporalUnit): LocalDateTime {
        if (unit == ChronoUnit.DAYS) return with(date, LocalTime.MIDNIGHT)
        return with(date, time.truncatedTo(unit))
    }

    actual fun plus(amountToAdd: TemporalAmount): LocalDateTime {
        if (amountToAdd is Period) {
            return with(date.plus(amountToAdd), time)
        }
        if (amountToAdd is Duration) {
            return plusSeconds(amountToAdd.seconds).plusNanos(amountToAdd.nano.toLong())
        }
        throw DateTimeException("Unsupported TemporalAmount for LocalDateTime: $amountToAdd")
    }

    actual fun plus(amountToAdd: Long, unit: TemporalUnit): LocalDateTime {
        if (unit is ChronoUnit) {
            return when (unit) {
                ChronoUnit.NANOS -> plusNanos(amountToAdd)
                ChronoUnit.MICROS -> plusDays(amountToAdd / 86_400_000_000L).plusNanos((amountToAdd % 86_400_000_000L) * 1000L)
                ChronoUnit.MILLIS -> plusDays(amountToAdd / 86_400_000L).plusNanos((amountToAdd % 86_400_000L) * 1_000_000L)
                ChronoUnit.SECONDS -> plusSeconds(amountToAdd)
                ChronoUnit.MINUTES -> plusMinutes(amountToAdd)
                ChronoUnit.HOURS -> plusHours(amountToAdd)
                ChronoUnit.HALF_DAYS -> plusDays(amountToAdd / 256L).plusHours((amountToAdd % 256L) * 12L)
                else -> with(date.plus(amountToAdd, unit), time)
            }
        }
        return unit.addTo(this, amountToAdd)
    }

    actual fun plusYears(years: Long): LocalDateTime = with(date.plusYears(years), time)
    actual fun plusMonths(months: Long): LocalDateTime = with(date.plusMonths(months), time)
    actual fun plusWeeks(weeks: Long): LocalDateTime = with(date.plusWeeks(weeks), time)
    actual fun plusDays(days: Long): LocalDateTime = with(date.plusDays(days), time)
    actual fun plusHours(hours: Long): LocalDateTime = plusWithOverflow(date, hours, 0, 0, 0, 1)
    actual fun plusMinutes(minutes: Long): LocalDateTime = plusWithOverflow(date, 0, minutes, 0, 0, 1)
    actual fun plusSeconds(seconds: Long): LocalDateTime = plusWithOverflow(date, 0, 0, seconds, 0, 1)
    actual fun plusNanos(nanos: Long): LocalDateTime = plusWithOverflow(date, 0, 0, 0, nanos, 1)

    actual fun minus(amountToSubtract: TemporalAmount): LocalDateTime {
        if (amountToSubtract is Period) {
            return with(date.minus(amountToSubtract), time)
        }
        if (amountToSubtract is Duration) {
            return minusSeconds(amountToSubtract.seconds).minusNanos(amountToSubtract.nano.toLong())
        }
        throw DateTimeException("Unsupported TemporalAmount for LocalDateTime: $amountToSubtract")
    }

    actual fun minus(amountToSubtract: Long, unit: TemporalUnit): LocalDateTime {
        return if (amountToSubtract == Long.MIN_VALUE) {
            plus(Long.MAX_VALUE, unit).plus(1, unit)
        } else {
            plus(-amountToSubtract, unit)
        }
    }

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

    actual fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        val end = from(endExclusive)
        if (unit is ChronoUnit) {
            if (unit.isTimeBased) {
                var amount = date.daysUntil(end.date)
                var timePart = end.time.toNanoOfDay() - time.toNanoOfDay()
                if (amount > 0 && timePart < 0) {
                    amount--
                    timePart += NANOS_PER_DAY
                } else if (amount < 0 && timePart > 0) {
                    amount++
                    timePart -= NANOS_PER_DAY
                }
                return when (unit) {
                    ChronoUnit.NANOS -> amount * NANOS_PER_DAY + timePart
                    ChronoUnit.MICROS -> amount * 86_400_000_000L + timePart / 1000L
                    ChronoUnit.MILLIS -> amount * 86_400_000L + timePart / 1_000_000L
                    ChronoUnit.SECONDS -> amount * SECONDS_PER_DAY + timePart / NANOS_PER_SECOND
                    ChronoUnit.MINUTES -> amount * MINUTES_PER_DAY + timePart / NANOS_PER_MINUTE
                    ChronoUnit.HOURS -> amount * HOURS_PER_DAY + timePart / NANOS_PER_HOUR
                    ChronoUnit.HALF_DAYS -> amount * 2L + timePart / (12 * NANOS_PER_HOUR)
                    else -> throw DateTimeException("Unsupported unit: $unit")
                }
            }
            var endDate = end.date
            if (endDate.isAfter(date) && end.time.isBefore(time)) {
                endDate = endDate.minusDays(1)
            } else if (endDate.isBefore(date) && end.time.isAfter(time)) {
                endDate = endDate.plusDays(1)
            }
            return date.until(endDate, unit)
        }
        return unit.between(this, end)
    }

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

    actual override fun adjustInto(temporal: Temporal): Temporal {
        return when (temporal) {
            is LocalDateTime -> this
            is OffsetDateTime -> OffsetDateTime.of(this, temporal.offset)
            is ZonedDateTime -> ZonedDateTime.of(this, temporal.zone)
            else -> throw DateTimeException("Unable to adjust temporal with LocalDateTime: $temporal")
        }
    }

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

        actual fun now(): LocalDateTime = now(Clock.systemDefaultZone())

        actual fun now(zone: ZoneId): LocalDateTime = now(Clock.system(zone))

        actual fun now(clock: Clock): LocalDateTime {
            val instant = clock.instant()
            return ofInstant(instant, clock.zone)
        }

        actual fun of(date: LocalDate, time: LocalTime): LocalDateTime = LocalDateTime(date, time)

        actual fun of(year: Int, month: Month, dayOfMonth: Int, hour: Int, minute: Int): LocalDateTime =
            of(year, month.value, dayOfMonth, hour, minute)

        actual fun of(year: Int, month: Month, dayOfMonth: Int, hour: Int, minute: Int, second: Int): LocalDateTime =
            of(year, month.value, dayOfMonth, hour, minute, second)

        actual fun of(year: Int, month: Month, dayOfMonth: Int, hour: Int, minute: Int, second: Int, nanoOfSecond: Int): LocalDateTime =
            of(year, month.value, dayOfMonth, hour, minute, second, nanoOfSecond)

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
