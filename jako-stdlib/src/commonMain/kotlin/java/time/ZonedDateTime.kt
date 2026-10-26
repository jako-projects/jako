@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAmount

expect class ZonedDateTime : Temporal, Serializable {
    val zone: ZoneId
    val offset: ZoneOffset
    val year: Int
    val month: Month
    val monthValue: Int
    val dayOfMonth: Int
    val dayOfYear: Int
    val dayOfWeek: DayOfWeek
    val hour: Int
    val minute: Int
    val second: Int
    val nano: Int

    fun getZone(): ZoneId
    fun getOffset(): ZoneOffset
    fun getYear(): Int
    fun getMonth(): Month
    fun getMonthValue(): Int
    fun getDayOfMonth(): Int
    fun getDayOfYear(): Int
    fun getDayOfWeek(): DayOfWeek
    fun getHour(): Int
    fun getMinute(): Int
    fun getSecond(): Int
    fun getNano(): Int

    fun withEarlierOffsetAtOverlap(): ZonedDateTime
    fun withLaterOffsetAtOverlap(): ZonedDateTime
    fun withZoneSameLocal(zone: ZoneId): ZonedDateTime
    fun withZoneSameInstant(zone: ZoneId): ZonedDateTime

    fun plus(amountToAdd: TemporalAmount): ZonedDateTime
    fun plusYears(years: Long): ZonedDateTime
    fun plusMonths(months: Long): ZonedDateTime
    fun plusWeeks(weeks: Long): ZonedDateTime
    fun plusDays(days: Long): ZonedDateTime
    fun plusHours(hours: Long): ZonedDateTime
    fun plusMinutes(minutes: Long): ZonedDateTime
    fun plusSeconds(seconds: Long): ZonedDateTime
    fun plusNanos(nanos: Long): ZonedDateTime

    fun minus(amountToSubtract: TemporalAmount): ZonedDateTime
    fun minusYears(years: Long): ZonedDateTime
    fun minusMonths(months: Long): ZonedDateTime
    fun minusWeeks(weeks: Long): ZonedDateTime
    fun minusDays(days: Long): ZonedDateTime
    fun minusHours(hours: Long): ZonedDateTime
    fun minusMinutes(minutes: Long): ZonedDateTime
    fun minusSeconds(seconds: Long): ZonedDateTime
    fun minusNanos(nanos: Long): ZonedDateTime

    fun toLocalDateTime(): LocalDateTime
    fun toLocalDate(): LocalDate
    fun toLocalTime(): LocalTime
    fun toInstant(): Instant
    fun toOffsetDateTime(): OffsetDateTime
    fun toEpochSecond(): Long
    fun format(formatter: DateTimeFormatter): String

    companion object {
        fun now(): ZonedDateTime
        fun now(zone: ZoneId): ZonedDateTime
        // [Nullability Decision] localDateTime and zone are non-null, matching JDK 8
        fun of(localDateTime: LocalDateTime, zone: ZoneId): ZonedDateTime
        fun of(date: LocalDate, time: LocalTime, zone: ZoneId): ZonedDateTime
        fun of(
            year: Int,
            month: Int,
            dayOfMonth: Int,
            hour: Int,
            minute: Int,
            second: Int,
            nanoOfSecond: Int,
            zone: ZoneId
        ): ZonedDateTime
        fun ofInstant(instant: Instant, zone: ZoneId): ZonedDateTime
        fun parse(text: CharSequence): ZonedDateTime
        fun parse(text: CharSequence, formatter: DateTimeFormatter): ZonedDateTime
        fun from(temporal: TemporalAccessor): ZonedDateTime
    }
}

