@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

import java.util.Date
import java.util.Locale

expect open class SimpleDateFormat : DateFormat {
    constructor(pattern: String)
    constructor(pattern: String, locale: Locale)
    val pattern: String
    val locale: Locale
    override fun parse(source: String, pos: ParsePosition): Date?
    override fun format(date: Date): String
}
