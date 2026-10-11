@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

expect class LocalTime : TemporalAccessor, Serializable, Comparable<LocalTime> {
    val hour: Int
    val minute: Int
    val second: Int
    val nano: Int

    fun getHour(): Int
    fun getMinute(): Int
    fun getSecond(): Int
    fun getNano(): Int
    fun toSecondOfDay(): Int
    fun toNanoOfDay(): Long
    fun plusHours(hoursToAdd: Long): LocalTime
    fun plusMinutes(minutesToAdd: Long): LocalTime
    fun plusSeconds(secondstoAdd: Long): LocalTime
    fun plusNanos(nanosToAdd: Long): LocalTime
    fun minusHours(hoursToSubtract: Long): LocalTime
    fun minusMinutes(minutesToSubtract: Long): LocalTime
    fun minusSeconds(secondsToSubtract: Long): LocalTime
    fun minusNanos(nanosToSubtract: Long): LocalTime
    // [Nullability Decision] formatter is non-null, matching JDK 8 LocalTime.format
    fun format(formatter: DateTimeFormatter): String
    fun isAfter(other: LocalTime): Boolean
    fun isBefore(other: LocalTime): Boolean
    override fun compareTo(other: LocalTime): Int

    companion object {
        val MIN: LocalTime
        val MAX: LocalTime
        val MIDNIGHT: LocalTime
        val NOON: LocalTime
        fun now(): LocalTime
        // [Nullability Decision] zone is non-null, matching JDK 8 LocalTime.now(ZoneId)
        fun now(zone: ZoneId): LocalTime
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
