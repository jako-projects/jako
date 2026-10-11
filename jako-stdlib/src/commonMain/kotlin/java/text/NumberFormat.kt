@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

import java.util.Locale

expect abstract class NumberFormat : Format {
    protected constructor()
    open fun format(number: Double): String
    open fun format(number: Long): String
    abstract fun format(number: Double, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder
    abstract fun format(number: Long, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder
    abstract fun parse(source: String, parsePosition: ParsePosition): Number?
    open fun parse(source: String): Number?
    open fun isGroupingUsed(): Boolean
    open fun setGroupingUsed(newValue: Boolean)
    open fun getMaximumIntegerDigits(): Int
    open fun setMaximumIntegerDigits(newValue: Int)
    open fun getMinimumIntegerDigits(): Int
    open fun setMinimumIntegerDigits(newValue: Int)
    open fun getMaximumFractionDigits(): Int
    open fun setMaximumFractionDigits(newValue: Int)
    open fun getMinimumFractionDigits(): Int
    open fun setMinimumFractionDigits(newValue: Int)

    companion object {
        fun getInstance(): NumberFormat
        fun getInstance(inLocale: Locale): NumberFormat
        fun getNumberInstance(): NumberFormat
        fun getNumberInstance(inLocale: Locale): NumberFormat
        fun getIntegerInstance(): NumberFormat
        fun getIntegerInstance(inLocale: Locale): NumberFormat
        fun getCurrencyInstance(): NumberFormat
        fun getCurrencyInstance(inLocale: Locale): NumberFormat
        fun getPercentInstance(): NumberFormat
        fun getPercentInstance(inLocale: Locale): NumberFormat
    }
}
