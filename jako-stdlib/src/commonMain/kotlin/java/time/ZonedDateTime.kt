@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor

expect class ZonedDateTime : Temporal, Serializable {
    val zone: ZoneId
    val offset: ZoneOffset
    fun getZone(): ZoneId
    fun getOffset(): ZoneOffset
    fun toLocalDateTime(): LocalDateTime
    fun toLocalDate(): LocalDate
    fun toLocalTime(): LocalTime
    fun toInstant(): Instant
    fun toOffsetDateTime(): OffsetDateTime
    fun toEpochSecond(): Long

    companion object {
        fun now(): ZonedDateTime
        fun now(zone: ZoneId): ZonedDateTime
        // [Nullability Decision] localDateTime and zone are non-null, matching JDK 8
        fun of(localDateTime: LocalDateTime, zone: ZoneId): ZonedDateTime
        fun of(date: LocalDate, time: LocalTime, zone: ZoneId): ZonedDateTime
        fun ofInstant(instant: Instant, zone: ZoneId): ZonedDateTime
        fun parse(text: CharSequence): ZonedDateTime
        fun from(temporal: TemporalAccessor): ZonedDateTime
    }
}
