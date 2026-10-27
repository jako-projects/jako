package java.text

import java.util.Date
import java.util.Locale
import java.util.TimeZone

actual abstract class DateFormat : Format() {
    actual var isLenient: Boolean = true
    actual var timeZone: TimeZone = TimeZone.getDefault()

    actual open fun setLenient(lenient: Boolean) {
        isLenient = lenient
    }

    actual open fun getTimeZone(): TimeZone = timeZone

    actual open fun setTimeZone(zone: TimeZone) {
        timeZone = zone
    }

    actual abstract fun parse(source: String, pos: ParsePosition): Date?
    actual abstract fun format(date: Date): String

    override fun format(obj: Any?): String = when (obj) {
        is Date -> format(obj)
        is Number -> format(Date(obj.toLong()))
        else -> throw IllegalArgumentException("Cannot format given Object as a Date")
    }

    override fun parseObject(source: String, pos: ParsePosition): Any? = parse(source, pos)

    @Throws(ParseException::class)
    actual fun parse(source: String): Date {
        val pos = ParsePosition(0)
        val result = parse(source, pos)
        if (result == null || pos.index == 0) {
            throw ParseException("Unparseable date: \"$source\"", pos.errorIndex)
        }
        return result
    }

    actual companion object {
        actual const val ERA_FIELD: Int = 0
        actual const val YEAR_FIELD: Int = 1
        actual const val MONTH_FIELD: Int = 2
        actual const val DATE_FIELD: Int = 3
        actual const val HOUR_OF_DAY1_FIELD: Int = 4
        actual const val HOUR_OF_DAY0_FIELD: Int = 5
        actual const val MINUTE_FIELD: Int = 6
        actual const val SECOND_FIELD: Int = 7
        actual const val MILLISECOND_FIELD: Int = 8

        actual const val FULL: Int = 0
        actual const val LONG: Int = 1
        actual const val MEDIUM: Int = 2
        actual const val SHORT: Int = 3
        actual const val DEFAULT: Int = MEDIUM

        actual fun getInstance(): DateFormat = getDateTimeInstance(SHORT, SHORT, Locale.getDefault())
        actual fun getDateInstance(): DateFormat = getDateInstance(DEFAULT, Locale.getDefault())
        actual fun getDateInstance(style: Int): DateFormat = getDateInstance(style, Locale.getDefault())
        actual fun getDateInstance(style: Int, aLocale: Locale): DateFormat {
            return SimpleDateFormat(datePatternFor(style, aLocale), aLocale)
        }

        actual fun getTimeInstance(): DateFormat = getTimeInstance(DEFAULT, Locale.getDefault())
        actual fun getTimeInstance(style: Int): DateFormat = getTimeInstance(style, Locale.getDefault())
        actual fun getTimeInstance(style: Int, aLocale: Locale): DateFormat {
            return SimpleDateFormat(timePatternFor(style, aLocale), aLocale)
        }

        actual fun getDateTimeInstance(): DateFormat = getDateTimeInstance(DEFAULT, DEFAULT, Locale.getDefault())
        actual fun getDateTimeInstance(dateStyle: Int, timeStyle: Int): DateFormat =
            getDateTimeInstance(dateStyle, timeStyle, Locale.getDefault())
        actual fun getDateTimeInstance(dateStyle: Int, timeStyle: Int, aLocale: Locale): DateFormat {
            return SimpleDateFormat("${datePatternFor(dateStyle, aLocale)} ${timePatternFor(timeStyle, aLocale)}", aLocale)
        }

        private fun datePatternFor(style: Int, locale: Locale): String {
            if (style !in FULL..SHORT) {
                throw IllegalArgumentException("Illegal date style $style")
            }
            return when (locale.language) {
                "zh" -> when (style) {
                    FULL -> "yyyy'年'M'月'd'日' EEEE"
                    LONG -> "yyyy'年'M'月'd'日'"
                    MEDIUM -> "yyyy-M-d"
                    else -> "yy-M-d"
                }
                else -> when (style) {
                    FULL -> "EEEE, MMMM d, yyyy"
                    LONG -> "MMMM d, yyyy"
                    MEDIUM -> "MMM d, yyyy"
                    else -> "M/d/yy"
                }
            }
        }

        private fun timePatternFor(style: Int, locale: Locale): String {
            if (style !in FULL..SHORT) {
                throw IllegalArgumentException("Illegal time style $style")
            }
            return when (locale.language) {
                "zh" -> when (style) {
                    FULL, LONG -> "a hh'时'mm'分'ss'秒'"
                    MEDIUM -> "H:mm:ss"
                    else -> "a h:mm"
                }
                else -> when (style) {
                    FULL, LONG -> "h:mm:ss a z"
                    MEDIUM -> "h:mm:ss a"
                    else -> "h:mm a"
                }
            }
        }
    }
}
