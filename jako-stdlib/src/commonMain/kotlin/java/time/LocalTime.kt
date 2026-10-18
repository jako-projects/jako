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

expect class LocalTime : Temporal, TemporalAdjuster, Serializable, Comparable<LocalTime> {
    val hour: Int
    val minute: Int
    val second: Int
    val nano: Int

    fun isSupported(field: TemporalField?): Boolean
    fun isSupported(unit: TemporalUnit?): Boolean
    fun range(field: TemporalField): ValueRange
    fun get(field: TemporalField): Int
    fun getLong(field: TemporalField): Long
    fun getHour(): Int
    fun getMinute(): Int
    fun getSecond(): Int
    fun getNano(): Int
    fun toSecondOfDay(): Int
    fun toNanoOfDay(): Long
    fun with(adjuster: TemporalAdjuster): LocalTime
    fun with(field: TemporalField, newValue: Long): LocalTime
    fun withHour(hour: Int): LocalTime
    fun withMinute(minute: Int): LocalTime
    fun withSecond(second: Int): LocalTime
    fun withNano(nanoOfSecond: Int): LocalTime
    fun truncatedTo(unit: TemporalUnit): LocalTime
    fun plus(amountToAdd: TemporalAmount): LocalTime
    fun plus(amountToAdd: Long, unit: TemporalUnit): LocalTime
    fun plusHours(hoursToAdd: Long): LocalTime
    fun plusMinutes(minutesToAdd: Long): LocalTime
    fun plusSeconds(secondstoAdd: Long): LocalTime
    fun plusNanos(nanosToAdd: Long): LocalTime
    fun minus(amountToSubtract: TemporalAmount): LocalTime
    fun minus(amountToSubtract: Long, unit: TemporalUnit): LocalTime
    fun minusHours(hoursToSubtract: Long): LocalTime
    fun minusMinutes(minutesToSubtract: Long): LocalTime
    fun minusSeconds(secondsToSubtract: Long): LocalTime
    fun minusNanos(nanosToSubtract: Long): LocalTime
    fun until(endExclusive: Temporal, unit: TemporalUnit): Long
    fun atDate(date: LocalDate): LocalDateTime
    fun atOffset(offset: ZoneOffset): OffsetTime
    // [Nullability Decision] formatter is non-null, matching JDK 8 LocalTime.format
    fun format(formatter: DateTimeFormatter): String
    fun isAfter(other: LocalTime): Boolean
    fun isBefore(other: LocalTime): Boolean
    override fun adjustInto(temporal: Temporal): Temporal
    override fun compareTo(other: LocalTime): Int

    companion object {
        val MIN: LocalTime
        val MAX: LocalTime
        val MIDNIGHT: LocalTime
        val NOON: LocalTime
        fun now(): LocalTime
        // [Nullability Decision] zone is non-null, matching JDK 8 LocalTime.now(ZoneId)
        fun now(zone: ZoneId): LocalTime
        fun now(clock: Clock): LocalTime
        fun of(hour: Int, minute: Int): LocalTime
        fun of(hour: Int, minute: Int, second: Int): LocalTime
        fun of(hour: Int, minute: Int, second: Int, nanoOfSecond: Int): LocalTime
        fun ofSecondOfDay(secondOfDay: Long): LocalTime
        fun ofNanoOfDay(nanoOfDay: Long): LocalTime
        // [Nullability Decision] text is non-null, matching JDK 8 LocalTime.parse
        fun parse(text: CharSequence): LocalTime
        fun parse(text: CharSequence, formatter: DateTimeFormatter): LocalTime
        fun from(temporal: TemporalAccessor): LocalTime
    }
}
