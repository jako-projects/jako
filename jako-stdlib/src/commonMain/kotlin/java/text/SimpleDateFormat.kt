@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

import java.util.Date
import java.util.Locale

expect open class SimpleDateFormat : DateFormat {
    constructor()
    constructor(pattern: String)
    constructor(pattern: String, locale: Locale)
    constructor(pattern: String, formatSymbols: DateFormatSymbols)

    open fun toPattern(): String
    open fun applyPattern(pattern: String)
    open fun getDateFormatSymbols(): DateFormatSymbols
    open fun setDateFormatSymbols(newFormatSymbols: DateFormatSymbols)

    override fun parse(source: String, pos: ParsePosition): Date?
    override fun format(date: Date): String
}

