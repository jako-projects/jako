@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.temporal.Temporal
import java.time.temporal.TemporalAmount
import java.time.temporal.TemporalUnit

expect class Duration : TemporalAmount, Serializable, Comparable<Duration> {
    val seconds: Long
    val nano: Int

    fun getSeconds(): Long
    fun getNano(): Int
    fun toDays(): Long
    fun toHours(): Long
    fun toMinutes(): Long
    fun toMillis(): Long
    fun toNanos(): Long
    fun plus(duration: Duration): Duration
    fun plusDays(daysToAdd: Long): Duration
    fun plusHours(hoursToAdd: Long): Duration
    fun plusMinutes(minutesToAdd: Long): Duration
    fun plusSeconds(secondsToAdd: Long): Duration
    fun plusMillis(millisToAdd: Long): Duration
    fun plusNanos(nanosToAdd: Long): Duration
    fun minus(duration: Duration): Duration
    fun minusDays(daysToSubtract: Long): Duration
    fun minusHours(hoursToSubtract: Long): Duration
    fun minusMinutes(minutesToSubtract: Long): Duration
    fun minusSeconds(secondsToSubtract: Long): Duration
    fun minusMillis(millisToSubtract: Long): Duration
    fun minusNanos(nanosToSubtract: Long): Duration
    fun multipliedBy(multiplicand: Long): Duration
    fun dividedBy(divisor: Long): Duration
    fun negated(): Duration
    fun abs(): Duration
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
        fun of(amount: Long, unit: TemporalUnit): Duration
        fun between(startInclusive: Temporal, endExclusive: Temporal): Duration
        fun parse(text: CharSequence): Duration
    }
}
