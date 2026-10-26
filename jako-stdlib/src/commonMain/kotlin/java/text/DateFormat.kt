@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

import java.util.Date
import java.util.Locale
import java.util.TimeZone

expect abstract class DateFormat : Format {
    var isLenient: Boolean
    var timeZone: TimeZone
    open fun setLenient(lenient: Boolean)
    open fun getTimeZone(): TimeZone
    open fun setTimeZone(zone: TimeZone)
    abstract fun parse(source: String, pos: ParsePosition): Date?
    abstract fun format(date: Date): String
    @Throws(ParseException::class)
    fun parse(source: String): Date

    companion object {
        val ERA_FIELD: Int
        val YEAR_FIELD: Int
        val MONTH_FIELD: Int
        val DATE_FIELD: Int
        val HOUR_OF_DAY1_FIELD: Int
        val HOUR_OF_DAY0_FIELD: Int
        val MINUTE_FIELD: Int
        val SECOND_FIELD: Int
        val MILLISECOND_FIELD: Int

        val FULL: Int
        val LONG: Int
        val MEDIUM: Int
        val SHORT: Int
        val DEFAULT: Int

        fun getInstance(): DateFormat
        fun getDateInstance(): DateFormat
        fun getDateInstance(style: Int): DateFormat
        fun getDateInstance(style: Int, aLocale: Locale): DateFormat
        fun getTimeInstance(): DateFormat
        fun getTimeInstance(style: Int): DateFormat
        fun getTimeInstance(style: Int, aLocale: Locale): DateFormat
        fun getDateTimeInstance(): DateFormat
        fun getDateTimeInstance(dateStyle: Int, timeStyle: Int): DateFormat
        fun getDateTimeInstance(dateStyle: Int, timeStyle: Int, aLocale: Locale): DateFormat
    }
}
