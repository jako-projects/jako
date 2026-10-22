@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAmount
import java.time.temporal.TemporalUnit

expect class Instant : Temporal, TemporalAdjuster, Serializable, Comparable<Instant> {
    val epochSecond: Long
    val nano: Int

    fun getEpochSecond(): Long
    fun getNano(): Int
    fun toEpochMilli(): Long
    fun truncatedTo(unit: TemporalUnit): Instant
    fun plus(amountToAdd: TemporalAmount): Instant
    fun plus(amountToAdd: Long, unit: TemporalUnit): Instant
    fun plusSeconds(secondsToAdd: Long): Instant
    fun plusMillis(millisToAdd: Long): Instant
    fun plusNanos(nanosToAdd: Long): Instant
    fun minus(amountToSubtract: TemporalAmount): Instant
    fun minus(amountToSubtract: Long, unit: TemporalUnit): Instant
    fun minusSeconds(secondsToSubtract: Long): Instant
    fun minusMillis(millisToSubtract: Long): Instant
    fun minusNanos(nanosToSubtract: Long): Instant
    fun until(endExclusive: Temporal, unit: TemporalUnit): Long
    fun atOffset(offset: ZoneOffset): OffsetDateTime
    fun atZone(zone: ZoneId): ZonedDateTime
    fun isAfter(otherInstant: Instant): Boolean
    fun isBefore(otherInstant: Instant): Boolean
    override fun adjustInto(temporal: Temporal): Temporal
    override fun compareTo(other: Instant): Int

    companion object {
        val EPOCH: Instant
        val MIN: Instant
        val MAX: Instant
        fun now(): Instant
        fun now(clock: Clock): Instant
        fun ofEpochMilli(epochMilli: Long): Instant
        fun ofEpochSecond(epochSecond: Long): Instant
        fun ofEpochSecond(epochSecond: Long, nanoAdjustment: Long): Instant
        // [Nullability Decision] text is non-null, matching JDK 8 Instant.parse(CharSequence)
        fun parse(text: CharSequence): Instant
        // [Nullability Decision] temporal is non-null, matching JDK 8 Instant.from(TemporalAccessor)
        fun from(temporal: TemporalAccessor): Instant
    }
}
