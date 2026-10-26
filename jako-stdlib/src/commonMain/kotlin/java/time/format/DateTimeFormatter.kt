@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time.format

import java.time.ZoneId
import java.time.temporal.TemporalAccessor
import java.util.Locale

expect class DateTimeFormatter {
    val locale: Locale
    val zone: ZoneId?
    fun getLocale(): Locale
    fun getZone(): ZoneId?
    fun withLocale(locale: Locale): DateTimeFormatter
    fun withZone(zone: ZoneId?): DateTimeFormatter
    // [Nullability Decision] temporal is non-null, matching JDK 8 DateTimeFormatter.format
    fun format(temporal: TemporalAccessor): String
    // [Nullability Decision] text is non-null, matching JDK 8 DateTimeFormatter.parse
    fun parse(text: CharSequence): TemporalAccessor

    companion object {
        val ISO_LOCAL_DATE: DateTimeFormatter
        val ISO_LOCAL_TIME: DateTimeFormatter
        val ISO_LOCAL_DATE_TIME: DateTimeFormatter
        val ISO_OFFSET_DATE_TIME: DateTimeFormatter
        val ISO_ZONED_DATE_TIME: DateTimeFormatter
        val ISO_DATE_TIME: DateTimeFormatter
        val ISO_INSTANT: DateTimeFormatter
        val BASIC_ISO_DATE: DateTimeFormatter

        // [Nullability Decision] pattern is non-null, matching JDK 8 DateTimeFormatter.ofPattern
        fun ofPattern(pattern: String): DateTimeFormatter
        fun ofPattern(pattern: String, locale: Locale): DateTimeFormatter
    }
}
