@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAmount

expect class Duration : TemporalAmount, Serializable, Comparable<Duration> {
    val seconds: Long
    val nano: Int

    fun getSeconds(): Long
    fun getNano(): Int
    fun toMillis(): Long
    fun toNanos(): Long
    fun plus(duration: Duration): Duration
    fun minus(duration: Duration): Duration
    fun isZero(): Boolean
    fun isNegative(): Boolean
    override fun compareTo(other: Duration): Int

    companion object {
        val ZERO: Duration
        fun ofMillis(millis: Long): Duration
        fun ofSeconds(seconds: Long): Duration
        fun ofSeconds(seconds: Long, nanoAdjustment: Long): Duration
        fun ofNanos(nanos: Long): Duration
        fun ofMinutes(minutes: Long): Duration
        fun ofHours(hours: Long): Duration
        fun ofDays(days: Long): Duration
    }
}

expect class Instant : Serializable, Comparable<Instant>, TemporalAccessor {
    val epochSecond: Long
    val nano: Int

    fun getEpochSecond(): Long
    fun getNano(): Int
    fun toEpochMilli(): Long
    fun plusSeconds(secondsToAdd: Long): Instant
    fun plusMillis(millisToAdd: Long): Instant
    fun plusNanos(nanosToAdd: Long): Instant
    fun minusSeconds(secondsToSubtract: Long): Instant
    fun minusMillis(millisToSubtract: Long): Instant
    fun minusNanos(nanosToSubtract: Long): Instant
    fun isAfter(otherInstant: Instant): Boolean
    fun isBefore(otherInstant: Instant): Boolean
    override fun compareTo(other: Instant): Int

    companion object {
        val EPOCH: Instant
        val MIN: Instant
        val MAX: Instant
        fun now(): Instant
        fun ofEpochMilli(epochMilli: Long): Instant
        fun ofEpochSecond(epochSecond: Long): Instant
        fun ofEpochSecond(epochSecond: Long, nanoAdjustment: Long): Instant
        // [Nullability Decision] text is non-null, matching JDK 8 Instant.parse(CharSequence)
        fun parse(text: CharSequence): Instant
        // [Nullability Decision] temporal is non-null, matching JDK 8 Instant.from(TemporalAccessor)
        fun from(temporal: TemporalAccessor): Instant
    }
}
