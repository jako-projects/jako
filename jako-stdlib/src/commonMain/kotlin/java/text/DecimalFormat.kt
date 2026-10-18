@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

expect open class DecimalFormat : NumberFormat {
    constructor()
    constructor(pattern: String)
    constructor(pattern: String, symbols: DecimalFormatSymbols)

    open fun applyPattern(pattern: String)
    open fun toPattern(): String
    open fun getDecimalFormatSymbols(): DecimalFormatSymbols
    open fun setDecimalFormatSymbols(newSymbols: DecimalFormatSymbols)

    override fun format(number: Double, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder
    override fun format(number: Long, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder
    override fun parse(source: String, parsePosition: ParsePosition): Number?
}

