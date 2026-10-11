@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

import java.util.Date
import java.util.TimeZone

expect abstract class DateFormat : Format {
    var isLenient: Boolean
    var timeZone: TimeZone
    abstract fun parse(source: String, pos: ParsePosition): Date?
    abstract fun format(date: Date): String
    @Throws(ParseException::class)
    fun parse(source: String): Date
}
