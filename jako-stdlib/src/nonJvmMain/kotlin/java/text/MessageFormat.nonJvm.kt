package java.text

import java.util.Date
import java.util.Locale

private sealed class MsgSegment
private data class MsgLiteral(val text: String) : MsgSegment()
private data class MsgPlaceholder(
    val argIndex: Int,
    val formatType: String?,
    val formatStyle: String?
) : MsgSegment()

actual open class MessageFormat : Format {
    private var pattern: String = ""
    private var locale: Locale = Locale.getDefault()
    private var segments: List<MsgSegment> = emptyList()

    actual constructor(pattern: String) : super() {
        this.locale = Locale.getDefault()
        applyPattern(pattern)
    }

    actual constructor(pattern: String, locale: Locale) : super() {
        this.locale = locale
        applyPattern(pattern)
    }

    actual open fun setLocale(locale: Locale) {
        this.locale = locale
    }

    actual open fun getLocale(): Locale = locale

    actual open fun applyPattern(pattern: String) {
        this.segments = compileMsgPattern(pattern)
        this.pattern = pattern
    }

    actual open fun toPattern(): String = pattern

    override fun format(obj: Any?): String {
        val args: Array<out Any?> = when (obj) {
            null -> emptyArray()
            is Array<*> -> obj
            else -> throw IllegalArgumentException("MessageFormat.format requires an Object[] array")
        }
        return formatSegments(args)
    }

    actual open fun parse(source: String, pos: ParsePosition): Array<Any?>? {
        var sIdx = pos.getIndex()
        val parsedMap = mutableMapOf<Int, Any?>()
        var maxArgIdx = -1

        for (segIdx in segments.indices) {
            when (val seg = segments[segIdx]) {
                is MsgLiteral -> {
                    val lit = seg.text
                    if (sIdx + lit.length > source.length || source.substring(sIdx, sIdx + lit.length) != lit) {
                        pos.setErrorIndex(sIdx)
                        return null
                    }
                    sIdx += lit.length
                }
                is MsgPlaceholder -> {
                    if (seg.argIndex > maxArgIdx) maxArgIdx = seg.argIndex
                    val nextLiteral = (segments.getOrNull(segIdx + 1) as? MsgLiteral)?.text
                    val subPos = ParsePosition(sIdx)
                    val value: Any? = when (seg.formatType) {
                        "number" -> {
                            val nf = createNumberSubformat(seg.formatStyle)
                            val n = nf.parse(source, subPos)
                            if (n == null) {
                                pos.setErrorIndex(sIdx)
                                return null
                            }
                            sIdx = subPos.getIndex()
                            n
                        }
                        "date", "time" -> {
                            val df = createDateTimeSubformat(seg.formatType, seg.formatStyle)
                            val d = df.parse(source, subPos)
                            if (d == null) {
                                pos.setErrorIndex(sIdx)
                                return null
                            }
                            sIdx = subPos.getIndex()
                            d
                        }
                        else -> {
                            val endIdx = if (!nextLiteral.isNullOrEmpty()) {
                                val found = source.indexOf(nextLiteral, sIdx)
                                if (found < 0) {
                                    pos.setErrorIndex(sIdx)
                                    return null
                                }
                                found
                            } else {
                                source.length
                            }
                            val strVal = source.substring(sIdx, endIdx)
                            sIdx = endIdx
                            strVal
                        }
                    }
                    parsedMap[seg.argIndex] = value
                }
            }
        }
        pos.setIndex(sIdx)
        val result = arrayOfNulls<Any>(maxArgIdx + 1)
        for ((k, v) in parsedMap) {
            result[k] = v
        }
        return result
    }

    @Throws(ParseException::class)
    actual open fun parse(source: String): Array<Any?> {
        val pos = ParsePosition(0)
        val result = parse(source, pos)
        if (result == null || (segments.isNotEmpty() && pos.getIndex() == 0 && source.isNotEmpty())) {
            throw ParseException("MessageFormat parse error!", pos.getErrorIndex())
        }
        return result
    }

    override fun parseObject(source: String, pos: ParsePosition): Any? = parse(source, pos)

    private fun formatSegments(arguments: Array<out Any?>): String {
        val out = StringBuilder()
        for (seg in segments) {
            when (seg) {
                is MsgLiteral -> out.append(seg.text)
                is MsgPlaceholder -> {
                    val idx = seg.argIndex
                    if (idx < 0 || idx >= arguments.size) {
                        out.append('{').append(idx).append('}')
                    } else {
                        val arg = arguments[idx]
                        if (arg == null) {
                            out.append("null")
                        } else {
                            out.append(formatArgument(arg, seg.formatType, seg.formatStyle, arguments))
                        }
                    }
                }
            }
        }
        return out.toString()
    }

    private fun formatArgument(
        arg: Any,
        formatType: String?,
        formatStyle: String?,
        allArguments: Array<out Any?>
    ): String {
        return when (formatType) {
            null -> when (arg) {
                is Number -> NumberFormat.getInstance(locale).format(arg)
                is Date -> DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).format(arg)
                else -> arg.toString()
            }
            "number" -> {
                if (arg !is Number) throw IllegalArgumentException("Cannot format given Object as a Number")
                createNumberSubformat(formatStyle).format(arg)
            }
            "date", "time" -> {
                val dateVal = when (arg) {
                    is Date -> arg
                    is Number -> Date(arg.toLong())
                    else -> throw IllegalArgumentException("Cannot format given Object as a Date")
                }
                createDateTimeSubformat(formatType, formatStyle).format(dateVal)
            }
            "choice" -> {
                if (arg !is Number) throw IllegalArgumentException("Cannot format given Object as a Number")
                val cf = ChoiceFormat(formatStyle ?: "")
                val chosen = cf.format(arg.toDouble())
                if (chosen.contains('{')) {
                    val subMf = MessageFormat(chosen, locale)
                    subMf.format(allArguments)
                } else {
                    chosen
                }
            }
            else -> throw IllegalArgumentException("unknown format type: $formatType")
        }
    }

    private fun createNumberSubformat(style: String?): NumberFormat {
        return when (val s = style?.trim() ?: "") {
            "", "null" -> NumberFormat.getInstance(locale)
            "integer" -> NumberFormat.getIntegerInstance(locale)
            "currency" -> NumberFormat.getCurrencyInstance(locale)
            "percent" -> NumberFormat.getPercentInstance(locale)
            else -> DecimalFormat(s, DecimalFormatSymbols.getInstance(locale))
        }
    }

    private fun createDateTimeSubformat(type: String, style: String?): DateFormat {
        val isDate = type == "date"
        return when (val s = style?.trim() ?: "") {
            "", "medium" -> if (isDate) DateFormat.getDateInstance(DateFormat.DEFAULT, locale)
            else DateFormat.getTimeInstance(DateFormat.DEFAULT, locale)
            "short" -> if (isDate) DateFormat.getDateInstance(DateFormat.SHORT, locale)
            else DateFormat.getTimeInstance(DateFormat.SHORT, locale)
            "long" -> if (isDate) DateFormat.getDateInstance(DateFormat.LONG, locale)
            else DateFormat.getTimeInstance(DateFormat.LONG, locale)
            "full" -> if (isDate) DateFormat.getDateInstance(DateFormat.FULL, locale)
            else DateFormat.getTimeInstance(DateFormat.FULL, locale)
            else -> SimpleDateFormat(s, locale)
        }
    }

    private fun compileMsgPattern(pat: String): List<MsgSegment> {
        val result = mutableListOf<MsgSegment>()
        val litBuf = StringBuilder()
        var i = 0
        var inQuote = false

        while (i < pat.length) {
            val ch = pat[i]
            if (inQuote) {
                if (ch == '\'') {
                    if (i + 1 < pat.length && pat[i + 1] == '\'') {
                        litBuf.append('\'')
                        i += 2
                    } else {
                        inQuote = false
                        i++
                    }
                } else {
                    litBuf.append(ch)
                    i++
                }
                continue
            }

            if (ch == '\'') {
                if (i + 1 < pat.length && pat[i + 1] == '\'') {
                    litBuf.append('\'')
                    i += 2
                } else {
                    inQuote = true
                    i++
                }
                continue
            }

            if (ch == '{') {
                if (litBuf.isNotEmpty()) {
                    result.add(MsgLiteral(litBuf.toString()))
                    litBuf.setLength(0)
                }
                var depth = 1
                var j = i + 1
                var braceInQuote = false
                while (j < pat.length && depth > 0) {
                    val c = pat[j]
                    if (c == '\'') {
                        braceInQuote = !braceInQuote
                    } else if (!braceInQuote) {
                        if (c == '{') depth++
                        else if (c == '}') depth--
                    }
                    j++
                }
                if (depth != 0) {
                    throw IllegalArgumentException("Unmatched braces in the pattern.")
                }
                val content = pat.substring(i + 1, j - 1)
                result.add(parsePlaceholderContent(content))
                i = j
                continue
            }

            litBuf.append(ch)
            i++
        }
        if (litBuf.isNotEmpty()) {
            result.add(MsgLiteral(litBuf.toString()))
        }
        return result
    }

    private fun parsePlaceholderContent(content: String): MsgPlaceholder {
        val firstComma = content.indexOf(',')
        if (firstComma < 0) {
            val idx = content.trim().toIntOrNull()
                ?: throw IllegalArgumentException("can't parse argument number: $content")
            if (idx < 0) {
                throw IllegalArgumentException("negative argument number: $idx")
            }
            return MsgPlaceholder(idx, null, null)
        }
        val idxStr = content.substring(0, firstComma).trim()
        val idx = idxStr.toIntOrNull()
            ?: throw IllegalArgumentException("can't parse argument number: $idxStr")
        if (idx < 0) {
            throw IllegalArgumentException("negative argument number: $idx")
        }

        val rest = content.substring(firstComma + 1)
        val secondComma = rest.indexOf(',')
        val rawType = (if (secondComma < 0) rest else rest.substring(0, secondComma)).trim()
        val style = if (secondComma < 0) null else rest.substring(secondComma + 1)

        if (rawType !in setOf("number", "date", "time", "choice")) {
            throw IllegalArgumentException("unknown format type: $rawType")
        }
        if (rawType == "choice" && style != null) {
            ChoiceFormat(style) // Validate ChoiceFormat pattern eagerly
        }
        return MsgPlaceholder(idx, rawType, style)
    }

    actual companion object {
        actual fun format(pattern: String, vararg arguments: Any?): String {
            return MessageFormat(pattern).format(arguments)
        }
    }
}
