@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

expect class LocalDateTime : TemporalAccessor, Serializable, Comparable<LocalDateTime> {
    val year: Int
    val month: Month
    val monthValue: Int
    val dayOfMonth: Int
    val hour: Int
    val minute: Int
    val second: Int
    val nano: Int

    fun getYear(): Int
    fun getMonth(): Month
    fun getMonthValue(): Int
    fun getDayOfMonth(): Int
    fun getHour(): Int
    fun getMinute(): Int
    fun getSecond(): Int
    fun getNano(): Int
    fun toLocalDate(): LocalDate
    fun toLocalTime(): LocalTime
    fun plusYears(years: Long): LocalDateTime
    fun plusMonths(months: Long): LocalDateTime
    fun plusWeeks(weeks: Long): LocalDateTime
    fun plusDays(days: Long): LocalDateTime
    fun plusHours(hours: Long): LocalDateTime
    fun plusMinutes(minutes: Long): LocalDateTime
    fun plusSeconds(seconds: Long): LocalDateTime
    fun plusNanos(nanos: Long): LocalDateTime
    fun minusYears(years: Long): LocalDateTime
    fun minusMonths(months: Long): LocalDateTime
    fun minusWeeks(weeks: Long): LocalDateTime
    fun minusDays(days: Long): LocalDateTime
    fun minusHours(hours: Long): LocalDateTime
    fun minusMinutes(minutes: Long): LocalDateTime
    fun minusSeconds(seconds: Long): LocalDateTime
    fun minusNanos(nanos: Long): LocalDateTime
    // [Nullability Decision] zone is non-null, matching JDK 8 LocalDateTime.atZone
    fun atZone(zone: ZoneId): ZonedDateTime
    fun atOffset(offset: ZoneOffset): OffsetDateTime
    fun toEpochSecond(offset: ZoneOffset): Long
    fun format(formatter: DateTimeFormatter): String
    fun isAfter(other: LocalDateTime): Boolean
    fun isBefore(other: LocalDateTime): Boolean
    override fun compareTo(other: LocalDateTime): Int

    companion object {
        val MIN: LocalDateTime
        val MAX: LocalDateTime
        fun now(): LocalDateTime
        fun now(zone: ZoneId): LocalDateTime
        // [Nullability Decision] date and time are non-null, matching JDK 8 LocalDateTime.of
        fun of(date: LocalDate, time: LocalTime): LocalDateTime
        fun of(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int): LocalDateTime
        fun of(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int, second: Int): LocalDateTime
        fun of(
            year: Int,
            month: Int,
            dayOfMonth: Int,
            hour: Int,
            minute: Int,
            second: Int,
            nanoOfSecond: Int
        ): LocalDateTime
        fun ofEpochSecond(epochSecond: Long, nanoOfSecond: Int, offset: ZoneOffset): LocalDateTime
        fun ofInstant(instant: Instant, zone: ZoneId): LocalDateTime
        // [Nullability Decision] text is non-null, matching JDK 8 LocalDateTime.parse
        fun parse(text: CharSequence): LocalDateTime
        fun parse(text: CharSequence, formatter: DateTimeFormatter): LocalDateTime
        fun from(temporal: TemporalAccessor): LocalDateTime
    }
}
