@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

import java.util.Locale

expect open class MessageFormat : Format {
    // [Nullability Decision] Java MessageFormat(String) pattern is non-null; null throws NPE.
    constructor(pattern: String)
    constructor(pattern: String, locale: Locale)
    open fun setLocale(locale: Locale)
    open fun getLocale(): Locale
    open fun applyPattern(pattern: String)
    open fun toPattern(): String
    open fun parse(source: String, pos: ParsePosition): Array<Any?>?
    @Throws(ParseException::class)
    open fun parse(source: String): Array<Any?>

    companion object {
        fun format(pattern: String, vararg arguments: Any?): String
    }
}
