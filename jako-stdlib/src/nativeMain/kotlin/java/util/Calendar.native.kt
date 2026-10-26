package java.util

import java.io.Serializable
import java.lang.Cloneable

internal fun Long.floorDiv(other: Long): Long {
    val r = this / other
    if ((this xor other) < 0L && (r * other != this)) {
        return r - 1L
    }
    return r
}

internal fun Long.floorMod(other: Long): Long {
    return this - this.floorDiv(other) * other
}

internal fun Int.floorDiv(other: Int): Int {
    val r = this / other
    if ((this xor other) < 0 && (r * other != this)) {
        return r - 1
    }
    return r
}

internal fun Int.floorMod(other: Int): Int {
    return this - this.floorDiv(other) * other
}

actual open class Calendar : Serializable, Cloneable, Comparable<Calendar> {
    actual var isLenient: Boolean = true
    private var timeInMillisVal: Long = 0L
    private var isFieldsSet = false

    private var year: Int = 1970
    private var month: Int = 0 // 0-based
    private var dayOfMonth: Int = 1
    private var hourOfDay: Int = 0
    private var minute: Int = 0
    private var second: Int = 0
    private var millisecond: Int = 0
    private var dayOfWeek: Int = 5 // Thursday = 5 (Sunday = 1)
    private var tz: TimeZone = TimeZone.getDefault()

    actual var timeInMillis: Long
        get() {
            if (!isFieldsSet) computeFields()
            return timeInMillisVal
        }
        set(value) {
            timeInMillisVal = value
            computeFields()
        }

    actual var time: Date
        get() = getTime()
        set(value) = setTime(value)

    actual var timeZone: TimeZone
        get() = getTimeZone()
        set(value) = setTimeZone(value)

    actual fun getTime(): Date = Date(timeInMillis)
    actual fun setTime(date: Date) {
        timeInMillis = date.time
    }

    actual fun getTimeInMillis(): Long = timeInMillis
    actual fun setTimeInMillis(millis: Long) {
        timeInMillis = millis
    }

    actual fun getTimeZone(): TimeZone = tz
    actual fun setTimeZone(value: TimeZone) {
        this.tz = value
        computeFields()
    }

    actual open fun get(field: Int): Int {
        if (!isFieldsSet) computeFields()
        return when (field) {
            ERA -> if (year >= 1) 1 else 0
            YEAR -> year
            MONTH -> month
            DAY_OF_MONTH, DATE -> dayOfMonth
            DAY_OF_WEEK -> dayOfWeek
            HOUR_OF_DAY -> hourOfDay
            HOUR -> hourOfDay % 12
            AM_PM -> if (hourOfDay >= 12) 1 else 0
            MINUTE -> minute
            SECOND -> second
            MILLISECOND -> millisecond
            ZONE_OFFSET -> tz.rawOffset
            DST_OFFSET -> tz.getOffset(timeInMillisVal) - tz.rawOffset
            else -> 0
        }
    }

    actual open fun set(field: Int, value: Int) {
        if (!isFieldsSet) computeFields()
        when (field) {
            YEAR -> year = value
            MONTH -> month = value
            DAY_OF_MONTH, DATE -> dayOfMonth = value
            HOUR_OF_DAY -> hourOfDay = value
            HOUR -> hourOfDay = (if (hourOfDay >= 12) 12 else 0) + (value % 12)
            AM_PM -> hourOfDay = (hourOfDay % 12) + (if (value == 1) 12 else 0)
            MINUTE -> minute = value
            SECOND -> second = value
            MILLISECOND -> millisecond = value
            DAY_OF_WEEK -> dayOfWeek = value
        }
        computeTime()
    }

    actual fun set(year: Int, month: Int, date: Int) {
        if (!isFieldsSet) computeFields()
        this.year = year
        this.month = month
        this.dayOfMonth = date
        computeTime()
    }

    actual fun set(year: Int, month: Int, date: Int, hourOfDay: Int, minute: Int) {
        if (!isFieldsSet) computeFields()
        this.year = year
        this.month = month
        this.dayOfMonth = date
        this.hourOfDay = hourOfDay
        this.minute = minute
        computeTime()
    }

    actual fun set(year: Int, month: Int, date: Int, hourOfDay: Int, minute: Int, second: Int) {
        if (!isFieldsSet) computeFields()
        this.year = year
        this.month = month
        this.dayOfMonth = date
        this.hourOfDay = hourOfDay
        this.minute = minute
        this.second = second
        computeTime()
    }

    actual fun add(field: Int, amount: Int) {
        if (!isFieldsSet) computeFields()
        when (field) {
            YEAR -> year += amount
            MONTH -> {
                val totalMonths = year * 12 + month + amount
                year = totalMonths.floorDiv(12)
                month = totalMonths.floorMod(12)
            }
            DAY_OF_MONTH, DATE -> dayOfMonth += amount
            HOUR_OF_DAY, HOUR -> hourOfDay += amount
            MINUTE -> minute += amount
            SECOND -> second += amount
            MILLISECOND -> millisecond += amount
        }
        computeTime()
    }

    actual override fun compareTo(other: Calendar): Int = timeInMillis.compareTo(other.timeInMillis)

    private fun computeFields() {
        val totalMillis = timeInMillisVal + tz.getOffset(timeInMillisVal)
        val seconds = totalMillis.floorDiv(1000L)
        millisecond = totalMillis.floorMod(1000L).toInt()

        val epochDays = seconds.floorDiv(86400L)
        val daySeconds = seconds.floorMod(86400L).toInt()

        hourOfDay = daySeconds / 3600
        minute = (daySeconds % 3600) / 60
        second = daySeconds % 60

        // Howard Hinnant algorithm
        var z = epochDays + 719468L
        val era = (if (z >= 0) z else z - 146096L) / 146097L
        val doe = (z - era * 146097L).toInt()
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        val y = yoe + era * 400L
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = doy - (153 * mp + 2) / 5 + 1
        val m = mp + if (mp < 10) 3 else -9
        year = (y + if (m <= 2) 1 else 0).toInt()
        month = m - 1
        dayOfMonth = d

        // Sunday = 1, Monday = 2, ..., Saturday = 7
        val dow = ((epochDays + 4L).floorMod(7L) + 7L).floorMod(7L) + 1L
        dayOfWeek = dow.toInt()
        isFieldsSet = true
    }

    private fun computeTime() {
        val totalMonths = year.toLong() * 12L + month.toLong()
        var y = totalMonths.floorDiv(12L)
        var m = totalMonths.floorMod(12L) + 1L
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val era = (if (y >= 0) y else y - 399L) / 400L
        val yoe = y - era * 400L
        val doy = (153 * (m - 3) + 2) / 5 + dayOfMonth - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        val epochDays = era * 146097L + doe - 719468L

        val dayMillis = (hourOfDay * 3600L + minute * 60L + second) * 1000L + millisecond
        val localMillis = epochDays * 86400000L + dayMillis
        val approxUtc = localMillis - tz.rawOffset
        val actualOffset = tz.getOffset(approxUtc)
        timeInMillisVal = localMillis - actualOffset
        computeFields()
    }

    actual companion object {
        actual const val ERA = 0
        actual const val YEAR = 1
        actual const val MONTH = 2
        actual const val WEEK_OF_YEAR = 3
        actual const val WEEK_OF_MONTH = 4
        actual const val DATE = 5
        actual const val DAY_OF_MONTH = 5
        actual const val DAY_OF_YEAR = 6
        actual const val DAY_OF_WEEK = 7
        actual const val DAY_OF_WEEK_IN_MONTH = 8
        actual const val AM_PM = 9
        actual const val HOUR = 10
        actual const val HOUR_OF_DAY = 11
        actual const val MINUTE = 12
        actual const val SECOND = 13
        actual const val MILLISECOND = 14
        actual const val ZONE_OFFSET = 15
        actual const val DST_OFFSET = 16

        actual const val JANUARY = 0
        actual const val FEBRUARY = 1
        actual const val MARCH = 2
        actual const val APRIL = 3
        actual const val MAY = 4
        actual const val JUNE = 5
        actual const val JULY = 6
        actual const val AUGUST = 7
        actual const val SEPTEMBER = 8
        actual const val OCTOBER = 9
        actual const val NOVEMBER = 10
        actual const val DECEMBER = 11

        actual const val SUNDAY = 1
        actual const val MONDAY = 2
        actual const val TUESDAY = 3
        actual const val WEDNESDAY = 4
        actual const val THURSDAY = 5
        actual const val FRIDAY = 6
        actual const val SATURDAY = 7

        actual fun getInstance(): Calendar = GregorianCalendar()
        actual fun getInstance(tz: TimeZone): Calendar = GregorianCalendar(tz)
    }
}
