@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

import java.util.Locale

expect open class MessageFormat : Format {
    // [Nullability Decision] Java MessageFormat(String) pattern is non-null; null throws NPE.
    constructor(pattern: String)
    constructor(pattern: String, locale: Locale)
    open fun applyPattern(pattern: String)
    open fun toPattern(): String

    companion object {
        fun format(pattern: String, vararg arguments: Any?): String
    }
}
