@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.temporal.TemporalAccessor

expect class OffsetDateTime : TemporalAccessor, Serializable, Comparable<OffsetDateTime> {
    val offset: ZoneOffset
    fun getOffset(): ZoneOffset
    fun toLocalDateTime(): LocalDateTime
    fun toLocalDate(): LocalDate
    fun toLocalTime(): LocalTime
    fun toInstant(): Instant
    fun toEpochSecond(): Long
    fun isAfter(other: OffsetDateTime): Boolean
    fun isBefore(other: OffsetDateTime): Boolean
    override fun compareTo(other: OffsetDateTime): Int

    companion object {
        val MIN: OffsetDateTime
        val MAX: OffsetDateTime
        fun now(): OffsetDateTime
        fun now(zone: ZoneId): OffsetDateTime
        // [Nullability Decision] dateTime and offset are non-null, matching JDK 8
        fun of(dateTime: LocalDateTime, offset: ZoneOffset): OffsetDateTime
        fun of(date: LocalDate, time: LocalTime, offset: ZoneOffset): OffsetDateTime
        fun ofInstant(instant: Instant, zone: ZoneId): OffsetDateTime
        fun parse(text: CharSequence): OffsetDateTime
        fun from(temporal: TemporalAccessor): OffsetDateTime
    }
}
