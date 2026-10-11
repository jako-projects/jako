@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time.format

import java.time.temporal.TemporalAccessor

expect class DateTimeFormatter {
    // [Nullability Decision] temporal is non-null, matching JDK 8 DateTimeFormatter.format
    fun format(temporal: TemporalAccessor): String
    // [Nullability Decision] text is non-null, matching JDK 8 DateTimeFormatter.parse
    fun parse(text: CharSequence): TemporalAccessor

    companion object {
        val ISO_LOCAL_DATE: DateTimeFormatter
        val ISO_LOCAL_TIME: DateTimeFormatter
        val ISO_LOCAL_DATE_TIME: DateTimeFormatter
        val ISO_INSTANT: DateTimeFormatter

        // [Nullability Decision] pattern is non-null, matching JDK 8 DateTimeFormatter.ofPattern
        fun ofPattern(pattern: String): DateTimeFormatter
    }
}
