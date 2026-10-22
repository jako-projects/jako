package java.text

import java.util.Locale

actual abstract class NumberFormat protected actual constructor() : Format() {
    private var groupingUsed: Boolean = true
    private var maxIntDigits: Int = 40
    private var minIntDigits: Int = 1
    private var maxFracDigits: Int = 3
    private var minFracDigits: Int = 0

    actual open fun format(number: Double): String = format(number, StringBuilder(), FieldPosition(0)).toString()
    actual open fun format(number: Long): String = format(number, StringBuilder(), FieldPosition(0)).toString()

    override fun format(obj: Any?): String {
        return when (obj) {
            is Number -> format(obj.toDouble())
            else -> throw IllegalArgumentException("Cannot format given Object as a Number")
        }
    }

    override fun parseObject(source: String, pos: ParsePosition): Any? = parse(source, pos)

    actual abstract fun format(number: Double, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder
    actual abstract fun format(number: Long, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder
    actual abstract fun parse(source: String, parsePosition: ParsePosition): Number?
    actual open fun parse(source: String): Number? = parse(source, ParsePosition(0))

    actual open fun isGroupingUsed(): Boolean = groupingUsed
    actual open fun setGroupingUsed(newValue: Boolean) { groupingUsed = newValue }
    actual open fun getMaximumIntegerDigits(): Int = maxIntDigits
    actual open fun setMaximumIntegerDigits(newValue: Int) { maxIntDigits = newValue }
    actual open fun getMinimumIntegerDigits(): Int = minIntDigits
    actual open fun setMinimumIntegerDigits(newValue: Int) { minIntDigits = newValue }
    actual open fun getMaximumFractionDigits(): Int = maxFracDigits
    actual open fun setMaximumFractionDigits(newValue: Int) { maxFracDigits = newValue }
    actual open fun getMinimumFractionDigits(): Int = minFracDigits
    actual open fun setMinimumFractionDigits(newValue: Int) { minFracDigits = newValue }

    actual companion object {
        actual fun getInstance(): NumberFormat = DecimalFormat("#,##0.###")
        actual fun getInstance(inLocale: Locale): NumberFormat = DecimalFormat("#,##0.###")
        actual fun getNumberInstance(): NumberFormat = DecimalFormat("#,##0.###")
        actual fun getNumberInstance(inLocale: Locale): NumberFormat = DecimalFormat("#,##0.###")
        actual fun getIntegerInstance(): NumberFormat = DecimalFormat("#,##0")
        actual fun getIntegerInstance(inLocale: Locale): NumberFormat = DecimalFormat("#,##0")
        actual fun getCurrencyInstance(): NumberFormat = DecimalFormat("¤#,##0.00")
        actual fun getCurrencyInstance(inLocale: Locale): NumberFormat = DecimalFormat("¤#,##0.00")
        actual fun getPercentInstance(): NumberFormat = DecimalFormat("#,##0%")
        actual fun getPercentInstance(inLocale: Locale): NumberFormat = DecimalFormat("#,##0%")
    }
}
