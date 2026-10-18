package java.text

import kotlin.math.abs
import kotlin.math.roundToLong

actual open class DecimalFormat : NumberFormat {
    private var patternStr: String = "#,##0.###"
    private var symbols: DecimalFormatSymbols = DecimalFormatSymbols.getInstance()

    actual constructor() : super()

    actual constructor(pattern: String) : super() {
        this.patternStr = pattern
    }

    actual constructor(pattern: String, symbols: DecimalFormatSymbols) : super() {
        this.patternStr = pattern
        this.symbols = symbols.clone() as DecimalFormatSymbols
    }

    actual open fun applyPattern(pattern: String) {
        this.patternStr = pattern
    }

    actual open fun toPattern(): String = patternStr

    actual open fun getDecimalFormatSymbols(): DecimalFormatSymbols =
        symbols.clone() as DecimalFormatSymbols

    actual open fun setDecimalFormatSymbols(newSymbols: DecimalFormatSymbols) {
        this.symbols = newSymbols.clone() as DecimalFormatSymbols
    }

    actual override fun format(number: Double, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder {
        toAppendTo.append(formatNumber(number))
        return toAppendTo
    }

    actual override fun format(number: Long, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder {
        toAppendTo.append(formatNumber(number.toDouble()))
        return toAppendTo
    }

    /**
     * [Nullability Decision] parse returns Number? (null if source cannot be parsed at parsePosition).
     */
    actual override fun parse(source: String, parsePosition: ParsePosition): Number? {
        val raw = source.substring(parsePosition.getIndex()).trim()
        if (raw.startsWith(symbols.getNaN())) {
            parsePosition.setIndex(parsePosition.getIndex() + symbols.getNaN().length)
            return Double.NaN
        }
        val groupSep = symbols.getGroupingSeparator().toString()
        val decSep = symbols.getDecimalSeparator()
        val minus = symbols.getMinusSign()
        val zero = symbols.getZeroDigit()

        val normalized = buildString {
            for (ch in raw) {
                when {
                    ch.toString() == groupSep -> {}
                    ch == decSep -> append('.')
                    ch == minus -> append('-')
                    zero != '0' && ch in zero..(zero + 9) -> append('0' + (ch - zero))
                    else -> append(ch)
                }
            }
        }
        val doubleVal = normalized.toDoubleOrNull()
        if (doubleVal != null) {
            parsePosition.setIndex(source.length)
            return if (doubleVal == doubleVal.roundToLong().toDouble()) doubleVal.toLong() else doubleVal
        }
        parsePosition.errorIndex = parsePosition.getIndex()
        return null
    }

    private fun formatNumber(number: Double): String {
        if (number.isNaN()) return symbols.getNaN()
        if (number.isInfinite()) {
            return if (number < 0) "${symbols.getMinusSign()}${symbols.getInfinity()}" else symbols.getInfinity()
        }

        var num = number
        var isPercent = false
        var isPerMill = false
        if (patternStr.contains('%')) {
            isPercent = true
            num *= 100
        } else if (patternStr.contains('\u2030')) {
            isPerMill = true
            num *= 1000
        }

        val isNegative = num < 0 || (1.0 / num < 0.0)
        num = abs(num)

        val dotIdx = patternStr.indexOf('.')
        val minFrac = if (dotIdx >= 0) {
            val fracPart = patternStr.substring(dotIdx + 1).replace("%", "").replace("\u2030", "").replace("¤", "")
            fracPart.count { it == '0' }
        } else 0

        val maxFrac = if (dotIdx >= 0) {
            val fracPart = patternStr.substring(dotIdx + 1).replace("%", "").replace("\u2030", "").replace("¤", "")
            fracPart.length
        } else 0

        val rounded = if (maxFrac > 0) {
            var factor = 1.0
            repeat(maxFrac) { factor *= 10.0 }
            (num * factor).roundToLong() / factor
        } else {
            num.roundToLong().toDouble()
        }

        val parts = rounded.toString().split('.')
        var intPart = parts[0]
        var fracPart = if (parts.size > 1) parts[1] else ""

        if (fracPart.length < minFrac) {
            fracPart = fracPart.padEnd(minFrac, '0')
        } else if (fracPart.length > maxFrac) {
            fracPart = fracPart.substring(0, maxFrac)
        }

        // Drop trailing zeroes beyond minFrac
        while (fracPart.length > minFrac && fracPart.endsWith('0')) {
            fracPart = fracPart.substring(0, fracPart.length - 1)
        }

        val zero = symbols.getZeroDigit()
        if (zero != '0') {
            intPart = intPart.map { if (it in '0'..'9') zero + (it - '0') else it }.joinToString("")
            fracPart = fracPart.map { if (it in '0'..'9') zero + (it - '0') else it }.joinToString("")
        }

        // Apply grouping
        if (isGroupingUsed() && patternStr.contains(',')) {
            val groupSize = if (patternStr.contains(',')) {
                val intPattern = if (dotIdx >= 0) patternStr.substring(0, dotIdx) else patternStr
                val lastComma = intPattern.lastIndexOf(',')
                val cleanAfter = intPattern.substring(lastComma + 1).count { it == '#' || it == '0' }
                if (cleanAfter > 0) cleanAfter else 3
            } else 3
            val sb = StringBuilder()
            val len = intPart.length
            for (i in 0 until len) {
                if (i > 0 && (len - i) % groupSize == 0) {
                    sb.append(symbols.getGroupingSeparator())
                }
                sb.append(intPart[i])
            }
            intPart = sb.toString()
        }

        val result = StringBuilder()
        if (isNegative) result.append(symbols.getMinusSign())
        result.append(intPart)
        if (fracPart.isNotEmpty()) {
            result.append(symbols.getDecimalSeparator()).append(fracPart)
        }
        if (isPercent) result.append(symbols.getPercent())
        if (isPerMill) result.append(symbols.getPerMill())
        return result.toString()
    }
}

