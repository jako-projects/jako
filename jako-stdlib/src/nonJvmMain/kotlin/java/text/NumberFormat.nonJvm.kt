package java.text

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.Locale

actual abstract class NumberFormat protected actual constructor() : Format() {
    private var groupingUsed: Boolean = true
    private var maxIntDigits: Int = 40
    private var minIntDigits: Int = 1
    private var maxFracDigits: Int = 3
    private var minFracDigits: Int = 0
    private var roundingModeVal: RoundingMode = RoundingMode.HALF_EVEN
    internal var parseIntegerOnly: Boolean = false

    actual open fun format(number: Double): String = format(number, StringBuilder(), FieldPosition(0)).toString()
    actual open fun format(number: Long): String = format(number, StringBuilder(), FieldPosition(0)).toString()

    override fun format(obj: Any?): String {
        return when (obj) {
            is Long -> format(obj)
            is Int, is Short, is Byte -> {
                val d = (obj as Number).toDouble()
                if (d % 1.0 == 0.0 && (d != 0.0 || 1.0 / d > 0.0) && d >= Long.MIN_VALUE.toDouble() && d <= Long.MAX_VALUE.toDouble()) {
                    format(obj.toLong())
                } else {
                    format(d)
                }
            }
            is BigInteger -> if (this is DecimalFormat) this.formatBigDecimal(BigDecimal(obj), StringBuilder(), FieldPosition(0)).toString() else format(obj.toLong())
            is BigDecimal -> if (this is DecimalFormat) this.formatBigDecimal(obj, StringBuilder(), FieldPosition(0)).toString() else format(obj.toDouble())
            is Number -> format(obj.toDouble())
            else -> throw IllegalArgumentException("Cannot format given Object as a Number")
        }
    }

    override fun parseObject(source: String, pos: ParsePosition): Any? = parse(source, pos)

    actual abstract fun format(number: Double, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder
    actual abstract fun format(number: Long, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder

    actual abstract fun parse(source: String, parsePosition: ParsePosition): Number?

    @Throws(ParseException::class)
    actual open fun parse(source: String): Number {
        val pos = ParsePosition(0)
        val result = parse(source, pos)
        if (result == null || pos.getIndex() == 0) {
            throw ParseException("Unparseable number: \"$source\"", pos.getErrorIndex())
        }
        return result
    }

    actual open fun isGroupingUsed(): Boolean = groupingUsed
    actual open fun setGroupingUsed(newValue: Boolean) { groupingUsed = newValue }
    actual open fun getMaximumIntegerDigits(): Int = maxIntDigits
    actual open fun setMaximumIntegerDigits(newValue: Int) { maxIntDigits = newValue.coerceAtLeast(0) }
    actual open fun getMinimumIntegerDigits(): Int = minIntDigits
    actual open fun setMinimumIntegerDigits(newValue: Int) { minIntDigits = newValue.coerceAtLeast(0) }
    actual open fun getMaximumFractionDigits(): Int = maxFracDigits
    actual open fun setMaximumFractionDigits(newValue: Int) {
        maxFracDigits = newValue.coerceAtLeast(0)
        if (minFracDigits > maxFracDigits) minFracDigits = maxFracDigits
    }
    actual open fun getMinimumFractionDigits(): Int = minFracDigits
    actual open fun setMinimumFractionDigits(newValue: Int) {
        minFracDigits = newValue.coerceAtLeast(0)
        if (maxFracDigits < minFracDigits) maxFracDigits = minFracDigits
    }
    actual open fun getRoundingMode(): RoundingMode = roundingModeVal
    actual open fun setRoundingMode(roundingMode: RoundingMode) { roundingModeVal = roundingMode }

    actual companion object {
        actual const val INTEGER_FIELD: Int = 0
        actual const val FRACTION_FIELD: Int = 1

        actual fun getInstance(): NumberFormat = getInstance(Locale.getDefault())
        actual fun getInstance(inLocale: Locale): NumberFormat =
            DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(inLocale))
        actual fun getNumberInstance(): NumberFormat = getNumberInstance(Locale.getDefault())
        actual fun getNumberInstance(inLocale: Locale): NumberFormat =
            DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(inLocale))
        actual fun getIntegerInstance(): NumberFormat = getIntegerInstance(Locale.getDefault())
        actual fun getIntegerInstance(inLocale: Locale): NumberFormat =
            DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(inLocale)).also { it.parseIntegerOnly = true }
        actual fun getCurrencyInstance(): NumberFormat = getCurrencyInstance(Locale.getDefault())
        actual fun getCurrencyInstance(inLocale: Locale): NumberFormat {
            val syms = DecimalFormatSymbols.getInstance(inLocale)
            val pattern = when (inLocale.getLanguage()) {
                "de", "fr" -> "#,##0.00\u00A0¤"
                "ja" -> "¤#,##0"
                else -> "¤#,##0.00"
            }
            return DecimalFormat(pattern, syms)
        }
        actual fun getPercentInstance(): NumberFormat = getPercentInstance(Locale.getDefault())
        actual fun getPercentInstance(inLocale: Locale): NumberFormat =
            DecimalFormat("#,##0%", DecimalFormatSymbols.getInstance(inLocale))
    }
}
