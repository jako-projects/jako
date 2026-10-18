@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAmount
import java.time.temporal.TemporalField
import java.time.temporal.TemporalUnit
import java.time.temporal.ValueRange

expect class LocalDateTime : Temporal, TemporalAdjuster, Serializable, Comparable<LocalDateTime> {
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

    fun isSupported(field: TemporalField?): Boolean
    fun isSupported(unit: TemporalUnit?): Boolean
    fun range(field: TemporalField): ValueRange
    fun get(field: TemporalField): Int
    fun getLong(field: TemporalField): Long
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
    fun toLocalDate(): LocalDate
    fun toLocalTime(): LocalTime
    fun with(adjuster: TemporalAdjuster): LocalDateTime
    fun with(field: TemporalField, newValue: Long): LocalDateTime
    fun withYear(year: Int): LocalDateTime
    fun withMonth(month: Int): LocalDateTime
    fun withDayOfMonth(dayOfMonth: Int): LocalDateTime
    fun withDayOfYear(dayOfYear: Int): LocalDateTime
    fun withHour(hour: Int): LocalDateTime
    fun withMinute(minute: Int): LocalDateTime
    fun withSecond(second: Int): LocalDateTime
    fun withNano(nanoOfSecond: Int): LocalDateTime
    fun truncatedTo(unit: TemporalUnit): LocalDateTime
    fun plus(amountToAdd: TemporalAmount): LocalDateTime
    fun plus(amountToAdd: Long, unit: TemporalUnit): LocalDateTime
    fun plusYears(years: Long): LocalDateTime
    fun plusMonths(months: Long): LocalDateTime
    fun plusWeeks(weeks: Long): LocalDateTime
    fun plusDays(days: Long): LocalDateTime
    fun plusHours(hours: Long): LocalDateTime
    fun plusMinutes(minutes: Long): LocalDateTime
    fun plusSeconds(seconds: Long): LocalDateTime
    fun plusNanos(nanos: Long): LocalDateTime
    fun minus(amountToSubtract: TemporalAmount): LocalDateTime
    fun minus(amountToSubtract: Long, unit: TemporalUnit): LocalDateTime
    fun minusYears(years: Long): LocalDateTime
    fun minusMonths(months: Long): LocalDateTime
    fun minusWeeks(weeks: Long): LocalDateTime
    fun minusDays(days: Long): LocalDateTime
    fun minusHours(hours: Long): LocalDateTime
    fun minusMinutes(minutes: Long): LocalDateTime
    fun minusSeconds(seconds: Long): LocalDateTime
    fun minusNanos(nanos: Long): LocalDateTime
    fun until(endExclusive: Temporal, unit: TemporalUnit): Long
    // [Nullability Decision] zone is non-null, matching JDK 8 LocalDateTime.atZone
    fun atZone(zone: ZoneId): ZonedDateTime
    fun atOffset(offset: ZoneOffset): OffsetDateTime
    fun toEpochSecond(offset: ZoneOffset): Long
    fun format(formatter: DateTimeFormatter): String
    fun isAfter(other: LocalDateTime): Boolean
    fun isBefore(other: LocalDateTime): Boolean
    override fun adjustInto(temporal: Temporal): Temporal
    override fun compareTo(other: LocalDateTime): Int

    companion object {
        val MIN: LocalDateTime
        val MAX: LocalDateTime
        fun now(): LocalDateTime
        fun now(zone: ZoneId): LocalDateTime
        fun now(clock: Clock): LocalDateTime
        // [Nullability Decision] date and time are non-null, matching JDK 8 LocalDateTime.of
        fun of(date: LocalDate, time: LocalTime): LocalDateTime
        fun of(year: Int, month: Month, dayOfMonth: Int, hour: Int, minute: Int): LocalDateTime
        fun of(year: Int, month: Month, dayOfMonth: Int, hour: Int, minute: Int, second: Int): LocalDateTime
        fun of(year: Int, month: Month, dayOfMonth: Int, hour: Int, minute: Int, second: Int, nanoOfSecond: Int): LocalDateTime
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
