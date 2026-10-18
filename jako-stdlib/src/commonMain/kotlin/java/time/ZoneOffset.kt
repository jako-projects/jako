@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster

expect class ZoneOffset : ZoneId, TemporalAccessor, TemporalAdjuster, Comparable<ZoneOffset>, Serializable {
    val totalSeconds: Int
    fun getTotalSeconds(): Int

    override val id: String
    override fun getId(): String
    override fun adjustInto(temporal: Temporal): Temporal
    override fun compareTo(other: ZoneOffset): Int

    companion object {
        val UTC: ZoneOffset
        val MIN: ZoneOffset
        val MAX: ZoneOffset

        // [Nullability Decision] offsetId is non-null, matching JDK 8 ZoneOffset.of(String)
        fun of(offsetId: String): ZoneOffset
        fun ofHours(hours: Int): ZoneOffset
        fun ofHoursMinutes(hours: Int, minutes: Int): ZoneOffset
        fun ofHoursMinutesSeconds(hours: Int, minutes: Int, seconds: Int): ZoneOffset
        fun ofTotalSeconds(totalSeconds: Int): ZoneOffset
    }
}
