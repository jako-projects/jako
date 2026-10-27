package java.text

actual open class ChoiceFormat : NumberFormat {
    private var choiceLimits: DoubleArray = DoubleArray(0)
    private var choiceFormats: Array<String> = emptyArray()

    actual constructor(newPattern: String) : super() {
        applyPattern(newPattern)
    }

    actual constructor(limits: DoubleArray, formats: Array<String>) : super() {
        setChoices(limits, formats)
    }

    actual open fun setChoices(limits: DoubleArray, formats: Array<String>) {
        if (limits.size != formats.size) {
            throw IllegalArgumentException("Array and limit arrays must be of the same length.")
        }
        choiceLimits = limits.copyOf()
        choiceFormats = formats.copyOf()
    }

    actual open fun getLimits(): DoubleArray = choiceLimits.copyOf()

    @Suppress("UNCHECKED_CAST")
    actual open fun getFormats(): Array<Any> = Array(choiceFormats.size) { choiceFormats[it] }

    actual open fun applyPattern(newPattern: String) {
        val limits = ArrayList<Double>()
        val formats = ArrayList<String>()
        val numBuf = StringBuilder()
        val fmtBuf = StringBuilder()
        var part = 0 // 0 = limit number, 1 = format text
        var sepChar = '#'
        var inQuote = false

        fun flushSegment() {
            if (part == 0) {
                if (numBuf.isEmpty()) return
                throw IllegalArgumentException("Invalid ChoiceFormat pattern: $newPattern")
            }
            val numStr = numBuf.toString().trim()
            val baseLimit = when (numStr) {
                "\u221E", "Infinity" -> Double.POSITIVE_INFINITY
                "-\u221E", "-Infinity" -> Double.NEGATIVE_INFINITY
                else -> numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid limit: $numStr")
            }
            val limit = if (sepChar == '<') nextDouble(baseLimit) else baseLimit
            if (limits.isNotEmpty() && limit < limits.last()) {
                throw IllegalArgumentException("ChoiceFormat limits must be in ascending order")
            }
            limits.add(limit)
            formats.add(fmtBuf.toString())
            numBuf.setLength(0)
            fmtBuf.setLength(0)
            part = 0
            sepChar = '#'
        }

        var i = 0
        while (i < newPattern.length) {
            val c = newPattern[i]
            if (c == '\'') {
                if (i + 1 < newPattern.length && newPattern[i + 1] == '\'') {
                    (if (part == 0) numBuf else fmtBuf).append('\'')
                    i += 2
                    continue
                }
                inQuote = !inQuote
                i++
                continue
            }
            if (inQuote) {
                (if (part == 0) numBuf else fmtBuf).append(c)
                i++
                continue
            }
            when {
                c == '<' || c == '#' || c == '\u2264' -> {
                    if (part != 0 || numBuf.isEmpty()) {
                        throw IllegalArgumentException("Invalid ChoiceFormat pattern: $newPattern")
                    }
                    sepChar = c
                    part = 1
                }
                c == '|' -> {
                    flushSegment()
                }
                else -> {
                    (if (part == 0) numBuf else fmtBuf).append(c)
                }
            }
            i++
        }
        if (part == 1 || numBuf.isNotEmpty()) {
            flushSegment()
        }
        choiceLimits = limits.toDoubleArray()
        choiceFormats = formats.toTypedArray()
    }

    actual open fun toPattern(): String {
        val sb = StringBuilder()
        for (i in choiceLimits.indices) {
            if (i > 0) sb.append('|')
            val lim = choiceLimits[i]
            val prev = previousDouble(lim)
            val tryLess = prev.toString()
            val tryHash = lim.toString()
            if (nextDouble(prev) == lim && tryLess.length < tryHash.length) {
                sb.append(tryLess).append('<')
            } else {
                when {
                    lim == Double.POSITIVE_INFINITY -> sb.append('\u221E')
                    lim == Double.NEGATIVE_INFINITY -> sb.append("-\u221E")
                    lim == lim.toLong().toDouble() -> sb.append("${lim.toLong()}.0")
                    else -> sb.append(tryHash)
                }
                sb.append('#')
            }
            sb.append(choiceFormats[i])
        }
        return sb.toString()
    }

    actual override fun format(number: Long, toAppendTo: StringBuilder, status: FieldPosition): StringBuilder =
        format(number.toDouble(), toAppendTo, status)

    actual override fun format(number: Double, toAppendTo: StringBuilder, status: FieldPosition): StringBuilder {
        if (choiceFormats.isEmpty()) return toAppendTo
        var idx = 0
        for (i in choiceLimits.indices) {
            if (!(number >= choiceLimits[i])) {
                break
            }
            idx = i
        }
        toAppendTo.append(choiceFormats[idx])
        return toAppendTo
    }

    /**
     * [Nullability Decision] parse returns Number (Double.NaN when no choice matches, matching JDK 8 ChoiceFormat).
     */
    actual override fun parse(text: String, status: ParsePosition): Number? {
        val start = status.getIndex()
        var furthest = start
        var bestNumber = Double.NaN
        for (i in choiceFormats.indices) {
            val fmt = choiceFormats[i]
            if (text.regionMatches(start, fmt, 0, fmt.length)) {
                val endIdx = start + fmt.length
                if (endIdx > furthest) {
                    furthest = endIdx
                    bestNumber = choiceLimits[i]
                }
            }
        }
        if (furthest > start) {
            status.setIndex(furthest)
        } else {
            status.errorIndex = start
        }
        return bestNumber
    }

    actual companion object {
        private const val SIGN = 0x8000000000000000UL
        private const val POSITIVEINFINITY = 0x7ff0000000000000L

        actual fun nextDouble(d: Double): Double = nextDouble(d, true)

        actual fun previousDouble(d: Double): Double = nextDouble(d, false)

        actual fun nextDouble(d: Double, positive: Boolean): Double {
            if (d.isNaN()) return d
            if (d == 0.0) {
                val smallest = Double.fromBits(1L)
                return if (positive) smallest else -smallest
            }
            val bits = d.toRawBits()
            val magnitude = bits and 0x7fffffffffffffffL
            val isPositive = bits >= 0L
            if (isPositive == positive) {
                if (magnitude == POSITIVEINFINITY) return d
                return Double.fromBits(bits + 1L)
            } else {
                return Double.fromBits(bits - 1L)
            }
        }
    }
}
