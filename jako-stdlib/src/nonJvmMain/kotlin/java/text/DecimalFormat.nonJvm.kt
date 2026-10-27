package java.text

import java.math.BigDecimal
import java.math.MathContext

actual open class DecimalFormat : NumberFormat {
    private var patternStr: String = "#,##0.###"
    private var symbols: DecimalFormatSymbols = DecimalFormatSymbols.getInstance()

    private var posPrefix: String = ""
    private var posSuffix: String = ""
    private var negPrefix: String = "-"
    private var negSuffix: String = ""
    private var hasExplicitNegSubpattern: Boolean = false
    private var multiplier: Int = 1
    private var isCurrencyFormat: Boolean = false
    private var groupingSize: Int = 3
    private var useExponentialNotation: Boolean = false
    private var minExponentDigits: Int = 0
    private var maxIntPatternDigits: Int = 1
    private var minIntPatternDigits: Int = 1

    actual constructor() : super() {
        applyPatternInternal(patternStr)
    }

    actual constructor(pattern: String) : super() {
        applyPatternInternal(pattern)
    }

    actual constructor(pattern: String, symbols: DecimalFormatSymbols) : super() {
        this.symbols = symbols.clone() as DecimalFormatSymbols
        applyPatternInternal(pattern)
    }

    actual open fun applyPattern(pattern: String) {
        applyPatternInternal(pattern)
    }

    actual open fun toPattern(): String = patternStr

    actual open fun getDecimalFormatSymbols(): DecimalFormatSymbols =
        symbols.clone() as DecimalFormatSymbols

    actual open fun setDecimalFormatSymbols(newSymbols: DecimalFormatSymbols) {
        this.symbols = newSymbols.clone() as DecimalFormatSymbols
    }

    private fun applyPatternInternal(pattern: String) {
        val subpatterns = splitSubpatterns(pattern)
        if (subpatterns.size > 2) {
            throw IllegalArgumentException("Malformed pattern \"$pattern\": too many ';' separators")
        }
        val posPat = subpatterns[0]
        parsePositiveSubpattern(posPat, pattern)
        if (subpatterns.size == 2) {
            hasExplicitNegSubpattern = true
            parseNegativeSubpattern(subpatterns[1], pattern)
        } else {
            hasExplicitNegSubpattern = false
            negPrefix = "\u0000-" + posPrefix
            negSuffix = posSuffix
        }
        this.patternStr = pattern
    }

    private fun splitSubpatterns(pattern: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuote = false
        var i = 0
        while (i < pattern.length) {
            val c = pattern[i]
            if (c == '\'') {
                if (i + 1 < pattern.length && pattern[i + 1] == '\'') {
                    sb.append("''")
                    i += 2
                    continue
                }
                inQuote = !inQuote
                sb.append(c)
                i++
            } else if (c == ';' && !inQuote) {
                result.add(sb.toString())
                sb.setLength(0)
                i++
            } else {
                sb.append(c)
                i++
            }
        }
        if (inQuote) {
            throw IllegalArgumentException("Unclosed quote in DecimalFormat pattern: \"$pattern\"")
        }
        result.add(sb.toString())
        return result
    }

    private fun parsePositiveSubpattern(sub: String, fullPattern: String) {
        val (prefixRaw, numberCore, suffixRaw) = extractAffixesAndCore(sub, fullPattern)
        posPrefix = unquoteAffix(prefixRaw)
        posSuffix = unquoteAffix(suffixRaw)

        multiplier = 1
        isCurrencyFormat = false
        if (posPrefix.contains('¤') || posSuffix.contains('¤')) {
            isCurrencyFormat = true
        }
        if (posPrefix.contains('%') || posSuffix.contains('%')) {
            multiplier = 100
        } else if (posPrefix.contains('\u2030') || posSuffix.contains('\u2030')) {
            multiplier = 1000
        }

        val expIdx = numberCore.indexOf('E')
        val mantissaCore: String
        if (expIdx >= 0) {
            useExponentialNotation = true
            mantissaCore = numberCore.substring(0, expIdx)
            val expPat = numberCore.substring(expIdx + 1)
            if (expPat.isEmpty() || expPat.any { it != '0' }) {
                throw IllegalArgumentException("Malformed exponential pattern \"$fullPattern\"")
            }
            if (mantissaCore.contains(',')) {
                throw IllegalArgumentException("Grouping separator not allowed in exponential pattern \"$fullPattern\"")
            }
            minExponentDigits = expPat.length
        } else {
            useExponentialNotation = false
            minExponentDigits = 0
            mantissaCore = numberCore
        }

        val firstDot = mantissaCore.indexOf('.')
        if (firstDot >= 0 && mantissaCore.indexOf('.', firstDot + 1) >= 0) {
            throw IllegalArgumentException("Multiple decimal separators in pattern \"$fullPattern\"")
        }

        val intPat = if (firstDot >= 0) mantissaCore.substring(0, firstDot) else mantissaCore
        val fracPat = if (firstDot >= 0) mantissaCore.substring(firstDot + 1) else ""

        // Validate integer part: '#' cannot follow '0'
        var seenZeroInInt = false
        var minInt = 0
        var maxIntPat = 0
        for (ch in intPat) {
            when (ch) {
                '0' -> {
                    seenZeroInInt = true
                    minInt++
                    maxIntPat++
                }
                '#' -> {
                    if (seenZeroInInt) {
                        throw IllegalArgumentException("Unexpected '#' after '0' in integer pattern \"$fullPattern\"")
                    }
                    maxIntPat++
                }
                ',' -> {}
                else -> throw IllegalArgumentException("Malformed number pattern \"$fullPattern\"")
            }
        }

        // Validate fraction part: '0' cannot follow '#'
        var seenHashInFrac = false
        var minFrac = 0
        var maxFrac = 0
        for (ch in fracPat) {
            when (ch) {
                '0' -> {
                    if (seenHashInFrac) {
                        throw IllegalArgumentException("Unexpected '0' after '#' in fraction pattern \"$fullPattern\"")
                    }
                    minFrac++
                    maxFrac++
                }
                '#' -> {
                    seenHashInFrac = true
                    maxFrac++
                }
                else -> throw IllegalArgumentException("Malformed fraction pattern \"$fullPattern\"")
            }
        }

        minIntPatternDigits = minInt
        maxIntPatternDigits = maxIntPat

        val lastComma = intPat.lastIndexOf(',')
        if (lastComma >= 0) {
            val digitsAfterComma = intPat.substring(lastComma + 1).count { it == '0' || it == '#' }
            if (digitsAfterComma == 0) {
                throw IllegalArgumentException("Trailing grouping separator in pattern \"$fullPattern\"")
            }
            setGroupingUsed(true)
            groupingSize = digitsAfterComma
        } else {
            setGroupingUsed(false)
            groupingSize = 0
        }

        setMinimumIntegerDigits(if (minInt > 0) minInt else if (minFrac > 0 && intPat.isEmpty()) 0 else 1)
        setMinimumFractionDigits(minFrac)
        setMaximumFractionDigits(maxFrac)
    }

    private fun parseNegativeSubpattern(sub: String, fullPattern: String) {
        val (prefixRaw, _, suffixRaw) = extractAffixesAndCore(sub, fullPattern)
        negPrefix = unquoteAffix(prefixRaw)
        negSuffix = unquoteAffix(suffixRaw)
    }

    private fun extractAffixesAndCore(sub: String, fullPattern: String): Triple<String, String, String> {
        var inQuote = false
        var coreStart = -1
        var coreEnd = -1
        var i = 0
        while (i < sub.length) {
            val c = sub[i]
            if (c == '\'') {
                if (i + 1 < sub.length && sub[i + 1] == '\'') {
                    i += 2
                    continue
                }
                inQuote = !inQuote
                i++
                continue
            }
            if (!inQuote && (c == '0' || c == '#' || c == '.' || c == ',')) {
                if (coreStart < 0) coreStart = i
                coreEnd = i + 1
            } else if (!inQuote && coreStart >= 0 && c == 'E') {
                var j = i + 1
                while (j < sub.length && (sub[j] == '0' || sub[j] == '#')) {
                    j++
                }
                coreEnd = j
                break
            } else if (!inQuote && coreStart >= 0) {
                break
            }
            i++
        }
        if (coreStart < 0) {
            throw IllegalArgumentException("Malformed pattern \"$fullPattern\": missing number digits")
        }
        return Triple(sub.substring(0, coreStart), sub.substring(coreStart, coreEnd), sub.substring(coreEnd))
    }

    private fun unquoteAffix(raw: String): String {
        val sb = StringBuilder()
        var inQuote = false
        var i = 0
        while (i < raw.length) {
            val c = raw[i]
            if (c == '\'') {
                if (i + 1 < raw.length && raw[i + 1] == '\'') {
                    sb.append('\'')
                    i += 2
                } else {
                    inQuote = !inQuote
                    i++
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    private fun expandAffix(affix: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < affix.length) {
            if (i + 1 < affix.length && affix[i] == '\u0000' && affix[i + 1] == '-') {
                sb.append(symbols.getMinusSign())
                i += 2
                continue
            }
            when (val c = affix[i]) {
                '¤' -> {
                    if (i + 1 < affix.length && affix[i + 1] == '¤') {
                        sb.append(symbols.getInternationalCurrencySymbol())
                        i += 2
                        continue
                    }
                    sb.append(symbols.getCurrencySymbol())
                }
                '%' -> sb.append(symbols.getPercent())
                '\u2030' -> sb.append(symbols.getPerMill())
                '-' -> sb.append(symbols.getMinusSign())
                else -> sb.append(c)
            }
            i++
        }
        return sb.toString()
    }

    actual override fun format(number: Double, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder {
        if (number.isNaN()) {
            toAppendTo.append(symbols.getNaN())
            return toAppendTo
        }
        if (number.isInfinite()) {
            val isNeg = number < 0
            val pref = expandAffix(if (isNeg) negPrefix else posPrefix)
            val suff = expandAffix(if (isNeg) negSuffix else posSuffix)
            toAppendTo.append(pref).append(symbols.getInfinity()).append(suff)
            return toAppendTo
        }
        val isNegative = number < 0.0 || (1.0 / number < 0.0)
        val absBd = BigDecimal.valueOf(kotlin.math.abs(number))
        return formatBigDecimalInternal(absBd, isNegative, toAppendTo, pos)
    }

    actual override fun format(number: Long, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder {
        val isNegative = number < 0L
        val absBd = if (number == Long.MIN_VALUE) {
            BigDecimal("9223372036854775808")
        } else {
            BigDecimal.valueOf(kotlin.math.abs(number))
        }
        return formatBigDecimalInternal(absBd, isNegative, toAppendTo, pos)
    }

    internal fun formatBigDecimal(bd: BigDecimal, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder {
        val isNeg = bd.signum() < 0
        return formatBigDecimalInternal(bd.abs(), isNeg, toAppendTo, pos)
    }

    private fun formatBigDecimalInternal(
        absValue: BigDecimal,
        isNegative: Boolean,
        toAppendTo: StringBuilder,
        pos: FieldPosition
    ): StringBuilder {
        val scaled = if (multiplier != 1) {
            absValue.multiply(BigDecimal.valueOf(multiplier.toLong()))
        } else {
            absValue
        }

        if (useExponentialNotation) {
            return formatExponential(scaled, isNegative, toAppendTo, pos)
        }

        val minFrac = getMinimumFractionDigits()
        val maxFrac = getMaximumFractionDigits()
        val minInt = getMinimumIntegerDigits()
        val maxInt = getMaximumIntegerDigits()

        val rounded = scaled.setScale(maxFrac, getRoundingMode())
        val plain = rounded.toPlainString()
        val dotIdx = plain.indexOf('.')
        var intDigits = if (dotIdx >= 0) plain.substring(0, dotIdx) else plain
        var fracDigits = if (dotIdx >= 0) plain.substring(dotIdx + 1) else ""

        // Strip trailing zeroes in fraction down to minFrac
        while (fracDigits.length > minFrac && fracDigits.endsWith('0')) {
            fracDigits = fracDigits.substring(0, fracDigits.length - 1)
        }
        if (fracDigits.length < minFrac) {
            fracDigits = fracDigits.padEnd(minFrac, '0')
        }

        // Apply integer min/max digit rules
        if (intDigits == "0" && minInt == 0 && fracDigits.isNotEmpty()) {
            intDigits = ""
        } else if (intDigits.length < minInt) {
            intDigits = intDigits.padStart(minInt, '0')
        }
        if (intDigits.length > maxInt) {
            intDigits = intDigits.substring(intDigits.length - maxInt)
        }

        // Localize digits with zeroDigit
        val zero = symbols.getZeroDigit()
        if (zero != '0') {
            intDigits = intDigits.map { if (it in '0'..'9') zero + (it - '0') else it }.joinToString("")
            fracDigits = fracDigits.map { if (it in '0'..'9') zero + (it - '0') else it }.joinToString("")
        }

        // Apply grouping
        if (isGroupingUsed() && groupingSize > 0 && intDigits.length > groupingSize) {
            val grouped = StringBuilder()
            val len = intDigits.length
            for (i in 0 until len) {
                if (i > 0 && (len - i) % groupingSize == 0) {
                    grouped.append(symbols.getGroupingSeparator())
                }
                grouped.append(intDigits[i])
            }
            intDigits = grouped.toString()
        }

        val prefixStr = expandAffix(if (isNegative) negPrefix else posPrefix)
        val suffixStr = expandAffix(if (isNegative) negSuffix else posSuffix)
        val decSep = if (isCurrencyFormat) symbols.getMonetaryDecimalSeparator() else symbols.getDecimalSeparator()

        toAppendTo.append(prefixStr)
        val intStart = toAppendTo.length
        toAppendTo.append(intDigits)
        val intEnd = toAppendTo.length

        val fracStart: Int
        val fracEnd: Int
        if (fracDigits.isNotEmpty()) {
            toAppendTo.append(decSep)
            fracStart = toAppendTo.length
            toAppendTo.append(fracDigits)
            fracEnd = toAppendTo.length
        } else {
            fracStart = intEnd
            fracEnd = intEnd
        }
        toAppendTo.append(suffixStr)

        if (pos.getField() == INTEGER_FIELD && pos.beginIndex == 0 && pos.endIndex == 0) {
            pos.setBeginIndex(intStart)
            pos.setEndIndex(intEnd)
        } else if (pos.getField() == FRACTION_FIELD && pos.beginIndex == 0 && pos.endIndex == 0) {
            pos.setBeginIndex(fracStart)
            pos.setEndIndex(fracEnd)
        }
        return toAppendTo
    }

    private fun formatExponential(
        scaled: BigDecimal,
        isNegative: Boolean,
        toAppendTo: StringBuilder,
        pos: FieldPosition
    ): StringBuilder {
        val minFrac = getMinimumFractionDigits()
        val maxFrac = getMaximumFractionDigits()
        val exponentMultiple = if (maxIntPatternDigits > minIntPatternDigits && maxIntPatternDigits > 1) {
            maxIntPatternDigits
        } else {
            1
        }
        val maxSigDigits = maxOf(1, maxIntPatternDigits + maxFrac)

        var intDigits: String
        var fracDigits: String
        val exp: Int

        if (scaled.signum() == 0) {
            val intCount = if (maxIntPatternDigits == 0) 0 else maxOf(1, minIntPatternDigits)
            intDigits = "0".repeat(intCount)
            fracDigits = "0".repeat(minFrac)
            exp = 0
        } else {
            val rounded = scaled.round(MathContext(maxSigDigits, getRoundingMode())).stripTrailingZeros()
            var coeff = rounded.unscaledValue().abs().toString()
            val baseExp = (coeff.length - 1) - rounded.scale()
            val intCount = if (exponentMultiple > 1) {
                val rem = ((baseExp % exponentMultiple) + exponentMultiple) % exponentMultiple
                rem + 1
            } else {
                if (maxIntPatternDigits == 0) 0 else maxOf(1, minIntPatternDigits)
            }
            exp = baseExp - (intCount - 1)
            val minTotalDigits = if (exponentMultiple > 1) minIntPatternDigits + minFrac else intCount + minFrac
            if (coeff.length < minTotalDigits) {
                coeff = coeff.padEnd(minTotalDigits, '0')
            }
            intDigits = if (intCount == 0) "" else coeff.substring(0, minOf(intCount, coeff.length)).padEnd(intCount, '0')
            fracDigits = if (coeff.length > intCount) coeff.substring(intCount) else ""
        }

        var expDigits = kotlin.math.abs(exp).toString().padStart(maxOf(1, minExponentDigits), '0')
        val zero = symbols.getZeroDigit()
        if (zero != '0') {
            intDigits = intDigits.map { if (it in '0'..'9') zero + (it - '0') else it }.joinToString("")
            fracDigits = fracDigits.map { if (it in '0'..'9') zero + (it - '0') else it }.joinToString("")
            expDigits = expDigits.map { if (it in '0'..'9') zero + (it - '0') else it }.joinToString("")
        }

        val prefixStr = expandAffix(if (isNegative) negPrefix else posPrefix)
        val suffixStr = expandAffix(if (isNegative) negSuffix else posSuffix)
        val decSep = if (isCurrencyFormat) symbols.getMonetaryDecimalSeparator() else symbols.getDecimalSeparator()

        toAppendTo.append(prefixStr)
        val intStart = toAppendTo.length
        toAppendTo.append(intDigits)
        val intEnd = toAppendTo.length

        val fracStart: Int
        val fracEnd: Int
        if (fracDigits.isNotEmpty()) {
            toAppendTo.append(decSep)
            fracStart = toAppendTo.length
            toAppendTo.append(fracDigits)
            fracEnd = toAppendTo.length
        } else {
            fracStart = intEnd
            fracEnd = intEnd
        }

        toAppendTo.append(symbols.getExponentSeparator())
        if (exp < 0) {
            toAppendTo.append(symbols.getMinusSign())
        }
        toAppendTo.append(expDigits)
        toAppendTo.append(suffixStr)

        if (pos.getField() == INTEGER_FIELD && pos.beginIndex == 0 && pos.endIndex == 0) {
            pos.setBeginIndex(intStart)
            pos.setEndIndex(intEnd)
        } else if (pos.getField() == FRACTION_FIELD && pos.beginIndex == 0 && pos.endIndex == 0) {
            pos.setBeginIndex(fracStart)
            pos.setEndIndex(fracEnd)
        }
        return toAppendTo
    }

    /**
     * [Nullability Decision] parse returns Number? (null if source cannot be parsed at parsePosition).
     */
    actual override fun parse(source: String, parsePosition: ParsePosition): Number? {
        val startIdx = parsePosition.getIndex()
        if (startIdx < 0 || startIdx >= source.length) {
            parsePosition.setErrorIndex(startIdx)
            return null
        }

        if (source.regionMatches(startIdx, symbols.getNaN(), 0, symbols.getNaN().length)) {
            parsePosition.setIndex(startIdx + symbols.getNaN().length)
            return Double.NaN
        }

        val expPosPref = expandAffix(posPrefix)
        val expNegPref = expandAffix(negPrefix)
        val expPosSuff = expandAffix(posSuffix)
        val expNegSuff = expandAffix(negSuffix)

        var idx = startIdx
        var isNegative = false
        var matchedNegAffix = false
        if (expNegPref.isNotEmpty() && source.regionMatches(idx, expNegPref, 0, expNegPref.length)) {
            isNegative = true
            matchedNegAffix = true
            idx += expNegPref.length
        } else if (expPosPref.isNotEmpty() && source.regionMatches(idx, expPosPref, 0, expPosPref.length)) {
            idx += expPosPref.length
        } else if (idx < source.length && source[idx] == symbols.getMinusSign()) {
            isNegative = true
            idx++
        } else if (idx < source.length && source[idx] == '+') {
            idx++
        }

        if (idx < source.length && source.regionMatches(idx, symbols.getInfinity(), 0, symbols.getInfinity().length)) {
            idx += symbols.getInfinity().length
            val expectedSuff = if (matchedNegAffix) expNegSuff else expPosSuff
            if (expectedSuff.isNotEmpty() && source.regionMatches(idx, expectedSuff, 0, expectedSuff.length)) {
                idx += expectedSuff.length
            }
            parsePosition.setIndex(idx)
            return if (isNegative) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
        }

        val groupSep = symbols.getGroupingSeparator()
        val decSep = if (isCurrencyFormat) symbols.getMonetaryDecimalSeparator() else symbols.getDecimalSeparator()
        val altDecSep = symbols.getDecimalSeparator()
        val zero = symbols.getZeroDigit()
        val expSep = symbols.getExponentSeparator()

        val numBuf = StringBuilder()
        if (isNegative) numBuf.append('-')
        var seenDecimal = false
        var seenDigits = false

        while (idx < source.length) {
            val ch = source[idx]
            when {
                ch in '0'..'9' -> {
                    numBuf.append(ch)
                    seenDigits = true
                    idx++
                }
                zero != '0' && ch in zero..(zero + 9) -> {
                    numBuf.append('0' + (ch - zero))
                    seenDigits = true
                    idx++
                }
                ch == groupSep && isGroupingUsed() && !seenDecimal -> {
                    idx++
                }
                (ch == decSep || ch == altDecSep) && !seenDecimal && !parseIntegerOnly -> {
                    seenDecimal = true
                    numBuf.append('.')
                    idx++
                }
                seenDigits && !parseIntegerOnly && (
                    (expSep.isNotEmpty() && source.regionMatches(idx, expSep, 0, expSep.length)) ||
                        ch == 'E' || ch == 'e'
                    ) -> {
                    val sepLen = if (expSep.isNotEmpty() && source.regionMatches(idx, expSep, 0, expSep.length)) expSep.length else 1
                    val nextIdx = idx + sepLen
                    if (nextIdx < source.length && (
                        source[nextIdx] in '0'..'9' ||
                            (zero != '0' && source[nextIdx] in zero..(zero + 9)) ||
                            source[nextIdx] == '+' ||
                            source[nextIdx] == '-' ||
                            source[nextIdx] == symbols.getMinusSign()
                        )
                    ) {
                        numBuf.append('E')
                        idx += sepLen
                        if (source[idx] == '+' || source[idx] == '-' || source[idx] == symbols.getMinusSign()) {
                            numBuf.append(if (source[idx] == '+') '+' else '-')
                            idx++
                        }
                    } else {
                        break
                    }
                }
                else -> break
            }
        }

        if (!seenDigits) {
            parsePosition.setErrorIndex(startIdx)
            return null
        }

        val expectedSuff = if (matchedNegAffix) expNegSuff else expPosSuff
        if (expectedSuff.isNotEmpty()) {
            if (source.regionMatches(idx, expectedSuff, 0, expectedSuff.length)) {
                idx += expectedSuff.length
            } else if (matchedNegAffix && hasExplicitNegSubpattern) {
                parsePosition.setErrorIndex(idx)
                return null
            }
        }

        parsePosition.setIndex(idx)
        val numStr = numBuf.toString()

        if (multiplier != 1) {
            val d = numStr.toDoubleOrNull() ?: return null
            val scaled = d / multiplier.toDouble()
            if (!seenDecimal && scaled == scaled.toLong().toDouble() && !(isNegative && scaled == 0.0)) {
                return scaled.toLong()
            }
            return scaled
        }

        if (!seenDecimal && !numStr.contains('E')) {
            if (isNegative && numStr.all { it == '-' || it == '0' }) {
                return -0.0
            }
            val longVal = numStr.toLongOrNull()
            if (longVal != null) return longVal
        }
        val doubleVal = numStr.toDoubleOrNull() ?: return null
        if (!seenDecimal && doubleVal == doubleVal.toLong().toDouble() && !(isNegative && doubleVal == 0.0)) {
            return doubleVal.toLong()
        }
        return doubleVal
    }
}
