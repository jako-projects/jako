package java.text

import kotlin.math.abs
import kotlin.math.roundToLong

actual open class DecimalFormat : NumberFormat {
    private var patternStr: String = "#,##0.###"

    actual constructor() : super()
    actual constructor(pattern: String) : super() {
        this.patternStr = pattern
    }

    actual open fun applyPattern(pattern: String) {
        this.patternStr = pattern
    }

    actual open fun toPattern(): String = patternStr

    actual override fun format(number: Double, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder {
        toAppendTo.append(formatNumber(number))
        return toAppendTo
    }

    actual override fun format(number: Long, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder {
        toAppendTo.append(formatNumber(number.toDouble()))
        return toAppendTo
    }

    actual override fun parse(source: String, parsePosition: ParsePosition): Number? {
        val str = source.substring(parsePosition.getIndex()).trim().replace(",", "")
        val doubleVal = str.toDoubleOrNull()
        if (doubleVal != null) {
            parsePosition.setIndex(source.length)
            return if (doubleVal == doubleVal.roundToLong().toDouble()) doubleVal.toLong() else doubleVal
        }
        parsePosition.errorIndex = parsePosition.getIndex()
        return null
    }

    private fun formatNumber(number: Double): String {
        var num = number
        var isPercent = false
        if (patternStr.contains('%')) {
            isPercent = true
            num *= 100
        }

        val isNegative = num < 0
        num = abs(num)

        val dotIdx = patternStr.indexOf('.')
        val minFrac = if (dotIdx >= 0) {
            val fracPart = patternStr.substring(dotIdx + 1).replace("%", "").replace("¤", "")
            fracPart.count { it == '0' }
        } else 0

        val maxFrac = if (dotIdx >= 0) {
            val fracPart = patternStr.substring(dotIdx + 1).replace("%", "").replace("¤", "")
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

        // Apply grouping
        if (isGroupingUsed() && patternStr.contains(',')) {
            val sb = StringBuilder()
            val len = intPart.length
            for (i in 0 until len) {
                if (i > 0 && (len - i) % 3 == 0) {
                    sb.append(',')
                }
                sb.append(intPart[i])
            }
            intPart = sb.toString()
        }

        val result = StringBuilder()
        if (isNegative) result.append('-')
        result.append(intPart)
        if (fracPart.isNotEmpty()) {
            result.append('.').append(fracPart)
        }
        if (isPercent) result.append('%')
        return result.toString()
    }
}
