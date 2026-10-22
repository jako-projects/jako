@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Cloneable

expect open class Calendar : Serializable, Cloneable, Comparable<Calendar> {
    var isLenient: Boolean
    var timeInMillis: Long
    var time: Date
    var timeZone: TimeZone

    open fun get(field: Int): Int
    open fun set(field: Int, value: Int)
    fun set(year: Int, month: Int, date: Int)
    fun set(year: Int, month: Int, date: Int, hourOfDay: Int, minute: Int)
    fun set(year: Int, month: Int, date: Int, hourOfDay: Int, minute: Int, second: Int)
    fun getTime(): Date
    fun setTime(date: Date)
    fun getTimeInMillis(): Long
    fun setTimeInMillis(millis: Long)
    fun getTimeZone(): TimeZone
    fun setTimeZone(value: TimeZone)
    fun add(field: Int, amount: Int)
    override fun compareTo(other: Calendar): Int

    companion object {
        val ERA: Int
        val YEAR: Int
        val MONTH: Int
        val WEEK_OF_YEAR: Int
        val WEEK_OF_MONTH: Int
        val DATE: Int
        val DAY_OF_MONTH: Int
        val DAY_OF_YEAR: Int
        val DAY_OF_WEEK: Int
        val DAY_OF_WEEK_IN_MONTH: Int
        val AM_PM: Int
        val HOUR: Int
        val HOUR_OF_DAY: Int
        val MINUTE: Int
        val SECOND: Int
        val MILLISECOND: Int
        val ZONE_OFFSET: Int
        val DST_OFFSET: Int

        val JANUARY: Int
        val FEBRUARY: Int
        val MARCH: Int
        val APRIL: Int
        val MAY: Int
        val JUNE: Int
        val JULY: Int
        val AUGUST: Int
        val SEPTEMBER: Int
        val OCTOBER: Int
        val NOVEMBER: Int
        val DECEMBER: Int

        val SUNDAY: Int
        val MONDAY: Int
        val TUESDAY: Int
        val WEDNESDAY: Int
        val THURSDAY: Int
        val FRIDAY: Int
        val SATURDAY: Int

        fun getInstance(): Calendar
        fun getInstance(tz: TimeZone): Calendar
    }
}
