@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalUnit

/**
 * A time with an offset from UTC/Greenwich in the ISO-8601 calendar system, such as 10:15:30+01:00.
 */
expect class OffsetTime : Temporal, TemporalAdjuster, Comparable<OffsetTime>, Serializable {
    val hour: Int
    val minute: Int
    val second: Int
    val nano: Int
    val offset: ZoneOffset

    fun getHour(): Int
    fun getMinute(): Int
    fun getSecond(): Int
    fun getNano(): Int
    fun getOffset(): ZoneOffset
    fun toLocalTime(): LocalTime
    fun withOffsetSameLocal(offset: ZoneOffset): OffsetTime
    fun withOffsetSameInstant(offset: ZoneOffset): OffsetTime
    fun plus(amountToAdd: Long, unit: TemporalUnit): OffsetTime
    fun plusHours(hours: Long): OffsetTime
    fun plusMinutes(minutes: Long): OffsetTime
    fun plusSeconds(seconds: Long): OffsetTime
    fun plusNanos(nanos: Long): OffsetTime
    fun minus(amountToSubtract: Long, unit: TemporalUnit): OffsetTime
    fun minusHours(hours: Long): OffsetTime
    fun minusMinutes(minutes: Long): OffsetTime
    fun minusSeconds(seconds: Long): OffsetTime
    fun minusNanos(nanos: Long): OffsetTime
    fun until(endExclusive: Temporal, unit: TemporalUnit): Long
    fun atDate(date: LocalDate): OffsetDateTime
    fun isAfter(other: OffsetTime): Boolean
    fun isBefore(other: OffsetTime): Boolean
    fun isEqual(other: OffsetTime): Boolean
    override fun adjustInto(temporal: Temporal): Temporal
    override fun compareTo(other: OffsetTime): Int

    companion object {
        val MIN: OffsetTime
        val MAX: OffsetTime
        fun now(): OffsetTime
        fun now(zone: ZoneId): OffsetTime
        fun now(clock: Clock): OffsetTime
        fun of(time: LocalTime, offset: ZoneOffset): OffsetTime
        fun of(hour: Int, minute: Int, second: Int, nanoOfSecond: Int, offset: ZoneOffset): OffsetTime
        fun ofInstant(instant: Instant, zone: ZoneId): OffsetTime
        fun parse(text: CharSequence): OffsetTime
        fun from(temporal: TemporalAccessor): OffsetTime
    }
}
