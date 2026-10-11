package java.text

import java.util.Date
import java.util.TimeZone

actual abstract class DateFormat : Format() {
    actual var isLenient: Boolean = true
    actual var timeZone: TimeZone = TimeZone.getDefault()

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
}
