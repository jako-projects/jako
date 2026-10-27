package java.text

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

private sealed class SdfToken
private data class SdfLiteral(val text: String) : SdfToken()
private data class SdfField(val letter: Char, val count: Int) : SdfToken()

private const val ALLOWED_SDF_PATTERN_CHARS = "GyYMwWDdFEuaHkKhmsSzZXL"

private val FALLBACK_SHORT_MONTHS_EN = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

private val FALLBACK_LONG_MONTHS_EN = arrayOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

actual open class SimpleDateFormat : DateFormat {
    private var patternStr: String
    private var symbols: DateFormatSymbols
    private var tokens: List<SdfToken> = emptyList()

    actual constructor() : this("M/d/yy h:mm a", Locale.getDefault())

    actual constructor(pattern: String) : this(pattern, Locale.getDefault())

    actual constructor(pattern: String, locale: Locale) : super() {
        this.patternStr = pattern
        this.symbols = DateFormatSymbols.getInstance(locale)
        this.tokens = compilePattern(pattern)
    }

    actual constructor(pattern: String, formatSymbols: DateFormatSymbols) : super() {
        this.patternStr = pattern
        this.symbols = formatSymbols.clone() as DateFormatSymbols
        this.tokens = compilePattern(pattern)
    }

    actual open fun toPattern(): String = patternStr

    actual open fun applyPattern(pattern: String) {
        this.tokens = compilePattern(pattern)
        this.patternStr = pattern
    }

    actual open fun getDateFormatSymbols(): DateFormatSymbols =
        symbols.clone() as DateFormatSymbols

    actual open fun setDateFormatSymbols(newFormatSymbols: DateFormatSymbols) {
        this.symbols = newFormatSymbols.clone() as DateFormatSymbols
    }

    private fun compilePattern(pattern: String): List<SdfToken> {
        val list = mutableListOf<SdfToken>()
        var i = 0
        while (i < pattern.length) {
            val c = pattern[i]
            when {
                c == '\'' -> {
                    if (i + 1 < pattern.length && pattern[i + 1] == '\'') {
                        list.add(SdfLiteral("'"))
                        i += 2
                    } else {
                        val sb = StringBuilder()
                        i++
                        var closed = false
                        while (i < pattern.length) {
                            val ch = pattern[i]
                            if (ch == '\'') {
                                if (i + 1 < pattern.length && pattern[i + 1] == '\'') {
                                    sb.append('\'')
                                    i += 2
                                } else {
                                    i++
                                    closed = true
                                    break
                                }
                            } else {
                                sb.append(ch)
                                i++
                            }
                        }
                        if (!closed) {
                            throw IllegalArgumentException("Unterminated quote in pattern: $pattern")
                        }
                        list.add(SdfLiteral(sb.toString()))
                    }
                }
                c in 'A'..'Z' || c in 'a'..'z' -> {
                    if (ALLOWED_SDF_PATTERN_CHARS.indexOf(c) < 0) {
                        throw IllegalArgumentException("Illegal pattern character '$c'")
                    }
                    var j = i + 1
                    while (j < pattern.length && pattern[j] == c) j++
                    val count = j - i
                    if (c == 'X' && count > 3) {
                        throw IllegalArgumentException("invalid ISO 8601 format: length=$count")
                    }
                    list.add(SdfField(c, count))
                    i = j
                }
                else -> {
                    val sb = StringBuilder()
                    while (i < pattern.length) {
                        val ch = pattern[i]
                        if (ch == '\'' || ch in 'A'..'Z' || ch in 'a'..'z') break
                        sb.append(ch)
                        i++
                    }
                    list.add(SdfLiteral(sb.toString()))
                }
            }
        }
        return list
    }

    actual override fun format(date: Date): String {
        return format(date, StringBuilder(), FieldPosition(0)).toString()
    }

    internal fun format(date: Date, toAppendTo: StringBuilder, pos: FieldPosition): StringBuilder {
        val cal = GregorianCalendar(timeZone)
        cal.time = date

        val year = cal.get(Calendar.YEAR)
        val era = if (date.time < -62135596800000L) 0 else 1
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun .. 7=Sat
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)
        val millis = cal.get(Calendar.MILLISECOND)
        val dayOfYear = computeDayOfYear(year, month + 1, day)

        val offsetMillis = timeZone.getOffset(date.time)
        val offsetMinutes = offsetMillis / 60000
        val sign = if (offsetMinutes >= 0) "+" else "-"
        val absMinutes = kotlin.math.abs(offsetMinutes)
        val hh = (absMinutes / 60).toString().padStart(2, '0')
        val mm = (absMinutes % 60).toString().padStart(2, '0')
        val tzRfc822 = "$sign$hh$mm"

        val eras = symbols.getEras()
        val monthsLong = symbols.getMonths()
        val monthsShort = symbols.getShortMonths()
        val daysLong = symbols.getWeekdays()
        val daysShort = symbols.getShortWeekdays()
        val amPm = symbols.getAmPmStrings()

        for (token in tokens) {
            when (token) {
                is SdfLiteral -> toAppendTo.append(token.text)
                is SdfField -> {
                    val begin = toAppendTo.length
                    val c = token.letter
                    val count = token.count
                    when (c) {
                        'G' -> toAppendTo.append(eras.getOrElse(era) { if (era == 1) "AD" else "BC" })
                        'y', 'Y' -> {
                            val dispYear = if (year <= 0) 1 - year else year
                            if (count == 2) {
                                toAppendTo.append((dispYear % 100).toString().padStart(2, '0'))
                            } else {
                                toAppendTo.append(dispYear.toString().padStart(count, '0'))
                            }
                        }
                        'M', 'L' -> {
                            when {
                                count >= 4 -> toAppendTo.append(monthsLong.getOrElse(month) { "" })
                                count == 3 -> toAppendTo.append(monthsShort.getOrElse(month) { "" })
                                else -> toAppendTo.append((month + 1).toString().padStart(count, '0'))
                            }
                        }
                        'd' -> toAppendTo.append(day.toString().padStart(count, '0'))
                        'D' -> toAppendTo.append(dayOfYear.toString().padStart(count, '0'))
                        'E' -> {
                            if (count >= 4) {
                                toAppendTo.append(daysLong.getOrElse(dayOfWeek) { "" })
                            } else {
                                toAppendTo.append(daysShort.getOrElse(dayOfWeek) { "" })
                            }
                        }
                        'u' -> {
                            val isoDow = if (dayOfWeek == 1) 7 else dayOfWeek - 1
                            toAppendTo.append(isoDow.toString().padStart(count, '0'))
                        }
                        'a' -> toAppendTo.append(amPm.getOrElse(if (hour < 12) 0 else 1) { "" })
                        'H' -> toAppendTo.append(hour.toString().padStart(count, '0'))
                        'k' -> {
                            val k = if (hour == 0) 24 else hour
                            toAppendTo.append(k.toString().padStart(count, '0'))
                        }
                        'K' -> toAppendTo.append((hour % 12).toString().padStart(count, '0'))
                        'h' -> {
                            val h12 = if (hour % 12 == 0) 12 else hour % 12
                            toAppendTo.append(h12.toString().padStart(count, '0'))
                        }
                        'm' -> toAppendTo.append(minute.toString().padStart(count, '0'))
                        's' -> toAppendTo.append(second.toString().padStart(count, '0'))
                        'S' -> toAppendTo.append(millis.toString().padStart(count, '0'))
                        'Z' -> toAppendTo.append(tzRfc822)
                        'X' -> {
                            if (offsetMinutes == 0) {
                                toAppendTo.append("Z")
                            } else {
                                when (count) {
                                    1 -> toAppendTo.append("$sign$hh")
                                    2 -> toAppendTo.append("$sign$hh$mm")
                                    else -> toAppendTo.append("$sign$hh:$mm")
                                }
                            }
                        }
                        'z' -> toAppendTo.append(timeZone.getID())
                        'w', 'W', 'F' -> toAppendTo.append(1.toString().padStart(count, '0'))
                    }
                    val end = toAppendTo.length
                    val fieldId = fieldIdForPatternLetter(c)
                    if (fieldId == pos.getField() && pos.beginIndex == 0 && pos.endIndex == 0) {
                        pos.setBeginIndex(begin)
                        pos.setEndIndex(end)
                    }
                }
            }
        }
        return toAppendTo
    }

    private fun fieldIdForPatternLetter(c: Char): Int = when (c) {
        'G' -> ERA_FIELD
        'y', 'Y' -> YEAR_FIELD
        'M', 'L' -> MONTH_FIELD
        'd' -> DATE_FIELD
        'k' -> HOUR_OF_DAY1_FIELD
        'H' -> HOUR_OF_DAY0_FIELD
        'm' -> MINUTE_FIELD
        's' -> SECOND_FIELD
        'S' -> MILLISECOND_FIELD
        else -> -1
    }

    /**
     * [Nullability Decision] parse returns Date? (null when date cannot be parsed at pos).
     */
    actual override fun parse(source: String, pos: ParsePosition): Date? {
        val startIndex = pos.getIndex()
        if (startIndex < 0 || startIndex > source.length) {
            pos.setErrorIndex(startIndex)
            return null
        }

        var idx = startIndex
        var era = 1
        var year = 1970
        var month = 1 // 1-based
        var day = 1
        var dayOfYear: Int? = null
        var hour24: Int? = null
        var clockHour24: Int? = null
        var hour12: Int? = null
        var clockHour12: Int? = null
        var amPm: Int? = null
        var min = 0
        var sec = 0
        var millis = 0
        var parsedTzOffsetMillis: Long? = null

        for (tIdx in tokens.indices) {
            val token = tokens[tIdx]
            when (token) {
                is SdfLiteral -> {
                    val lit = token.text
                    if (idx + lit.length > source.length || source.substring(idx, idx + lit.length) != lit) {
                        pos.setErrorIndex(idx)
                        return null
                    }
                    idx += lit.length
                }
                is SdfField -> {
                    val c = token.letter
                    val count = token.count
                    val subsequentFixedDigits = computeSubsequentFixedDigits(tIdx + 1)
                    when (c) {
                        'G' -> {
                            val eras = symbols.getEras()
                            var matched = false
                            for (eIdx in eras.indices) {
                                val eStr = eras[eIdx]
                                if (eStr.isNotEmpty() && source.regionMatches(idx, eStr, 0, eStr.length, ignoreCase = true)) {
                                    era = eIdx
                                    idx += eStr.length
                                    matched = true
                                    break
                                }
                            }
                            if (!matched) {
                                pos.setErrorIndex(idx)
                                return null
                            }
                        }
                        'M', 'L' -> {
                            if (count >= 3) {
                                val mPair = matchMonthText(source, idx)
                                if (mPair == null) {
                                    pos.setErrorIndex(idx)
                                    return null
                                }
                                month = mPair.first
                                idx = mPair.second
                            } else {
                                val numPair = parseNumberAt(source, idx, count, subsequentFixedDigits, allowNegative = false)
                                if (numPair == null) {
                                    pos.setErrorIndex(idx)
                                    return null
                                }
                                month = numPair.first
                                idx = numPair.second
                            }
                        }
                        'E' -> {
                            val wEnd = matchWeekdayText(source, idx)
                            if (wEnd < 0) {
                                pos.setErrorIndex(idx)
                                return null
                            }
                            idx = wEnd
                        }
                        'a' -> {
                            val apPair = matchAmPmText(source, idx)
                            if (apPair == null) {
                                pos.setErrorIndex(idx)
                                return null
                            }
                            amPm = apPair.first
                            idx = apPair.second
                        }
                        'Z', 'X' -> {
                            val tzPair = parseOffsetToken(source, idx, c)
                            if (tzPair == null) {
                                pos.setErrorIndex(idx)
                                return null
                            }
                            parsedTzOffsetMillis = tzPair.first
                            idx = tzPair.second
                        }
                        'z' -> {
                            val tzPair = parseZoneNameToken(source, idx)
                            if (tzPair == null) {
                                pos.setErrorIndex(idx)
                                return null
                            }
                            parsedTzOffsetMillis = tzPair.first
                            idx = tzPair.second
                        }
                        else -> {
                            val numPair = parseNumberAt(source, idx, count, subsequentFixedDigits, allowNegative = (c == 'y' || c == 'Y') && isLenient)
                            if (numPair == null) {
                                pos.setErrorIndex(idx)
                                return null
                            }
                            val v = numPair.first
                            val consumed = numPair.second - idx
                            idx = numPair.second
                            when (c) {
                                'y', 'Y' -> {
                                    year = if (count <= 2 && consumed == 2 && v >= 0) {
                                        if (v < 70) 2000 + v else 1900 + v
                                    } else {
                                        v
                                    }
                                }
                                'd' -> day = v
                                'D' -> dayOfYear = v
                                'H' -> hour24 = v
                                'k' -> clockHour24 = v
                                'K' -> hour12 = v
                                'h' -> clockHour12 = v
                                'm' -> min = v
                                's' -> sec = v
                                'S' -> millis = v
                            }
                        }
                    }
                }
            }
        }

        if (era == 0 && year > 0) {
            year = 1 - year
        }

        // Strict validation when !isLenient
        if (!isLenient) {
            if (month !in 1..12) {
                pos.setErrorIndex(startIndex)
                return null
            }
            val maxDom = daysInMonth(year, month)
            if (day !in 1..maxDom) {
                pos.setErrorIndex(startIndex)
                return null
            }
            if (dayOfYear != null) {
                val maxDoy = if (isGregorianLeap(year)) 366 else 365
                if (dayOfYear !in 1..maxDoy) {
                    pos.setErrorIndex(startIndex)
                    return null
                }
            }
            if (hour24 != null && hour24 !in 0..23) {
                pos.setErrorIndex(startIndex)
                return null
            }
            if (clockHour24 != null && clockHour24 !in 1..24) {
                pos.setErrorIndex(startIndex)
                return null
            }
            if (hour12 != null && hour12 !in 0..11) {
                pos.setErrorIndex(startIndex)
                return null
            }
            if (clockHour12 != null && clockHour12 !in 1..12) {
                pos.setErrorIndex(startIndex)
                return null
            }
            if (min !in 0..59 || sec !in 0..59 || millis !in 0..999) {
                pos.setErrorIndex(startIndex)
                return null
            }
        }

        val resolvedHour = when {
            hour24 != null -> hour24
            clockHour24 != null -> if (clockHour24 == 24) 0 else clockHour24
            clockHour12 != null -> {
                val base = if (clockHour12 == 12) 0 else clockHour12
                base + (amPm ?: 0) * 12
            }
            hour12 != null -> hour12 + (amPm ?: 0) * 12
            else -> if (amPm == 1) 12 else 0
        }

        if (dayOfYear != null) {
            month = 1
            day = dayOfYear
        }

        val localMillis = computeLocalEpochMillis(year, month - 1, day, resolvedHour, min, sec, millis)
        val effectiveOffset = parsedTzOffsetMillis ?: run {
            val approxUtc = localMillis - timeZone.rawOffset.toLong()
            timeZone.getOffset(approxUtc).toLong()
        }
        pos.setIndex(idx)
        return Date(localMillis - effectiveOffset)
    }

    private fun isNumericSdfField(f: SdfField): Boolean {
        return when (f.letter) {
            'y', 'Y', 'd', 'D', 'H', 'k', 'K', 'h', 'm', 's', 'S', 'u', 'w', 'W', 'F' -> true
            'M', 'L' -> f.count <= 2
            else -> false
        }
    }

    private fun computeSubsequentFixedDigits(startTokenIdx: Int): Int {
        var sum = 0
        var i = startTokenIdx
        while (i < tokens.size) {
            val t = tokens[i]
            if (t is SdfField && isNumericSdfField(t)) {
                sum += t.count
                i++
            } else {
                break
            }
        }
        return sum
    }

    private fun parseNumberAt(
        source: String,
        start: Int,
        tokenCount: Int,
        subsequentFixedDigits: Int,
        allowNegative: Boolean
    ): Pair<Int, Int>? {
        if (start >= source.length) return null
        var i = start
        if (allowNegative && (source[i] == '-' || source[i] == '+')) {
            i++
        }
        val digitStart = i
        var avail = 0
        while (digitStart + avail < source.length && source[digitStart + avail] in '0'..'9') {
            avail++
        }
        if (avail == 0) return null
        val take = if (subsequentFixedDigits > 0) {
            tokenCount.coerceAtMost(avail - subsequentFixedDigits)
        } else {
            avail
        }
        if (take <= 0) return null
        val end = digitStart + take
        val num = source.substring(start, end).toIntOrNull() ?: return null
        return num to end
    }

    private fun matchMonthText(source: String, start: Int): Pair<Int, Int>? {
        val candidates = listOf(
            symbols.getMonths(),
            symbols.getShortMonths(),
            FALLBACK_LONG_MONTHS_EN,
            FALLBACK_SHORT_MONTHS_EN
        )
        // Match longest first so "January" beats "Jan"
        var bestMonth = -1
        var bestLen = 0
        for (arr in candidates) {
            for (mIdx in 0 until minOf(12, arr.size)) {
                val name = arr[mIdx]
                if (name.isNotEmpty() && name.length > bestLen &&
                    source.regionMatches(start, name, 0, name.length, ignoreCase = true)
                ) {
                    bestMonth = mIdx + 1
                    bestLen = name.length
                }
            }
        }
        if (bestMonth > 0) return bestMonth to (start + bestLen)
        return null
    }

    private fun matchWeekdayText(source: String, start: Int): Int {
        val candidates = listOf(
            symbols.getWeekdays(),
            symbols.getShortWeekdays(),
            arrayOf("", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"),
            arrayOf("", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        )
        var bestLen = -1
        for (arr in candidates) {
            for (name in arr) {
                if (name.isNotEmpty() && name.length > bestLen &&
                    source.regionMatches(start, name, 0, name.length, ignoreCase = true)
                ) {
                    bestLen = name.length
                }
            }
        }
        return if (bestLen > 0) start + bestLen else -1
    }

    private fun matchAmPmText(source: String, start: Int): Pair<Int, Int>? {
        val ampms = symbols.getAmPmStrings()
        for (idx in ampms.indices) {
            val s = ampms[idx]
            if (s.isNotEmpty() && source.regionMatches(start, s, 0, s.length, ignoreCase = true)) {
                return idx to (start + s.length)
            }
        }
        if (source.regionMatches(start, "AM", 0, 2, ignoreCase = true)) return 0 to (start + 2)
        if (source.regionMatches(start, "PM", 0, 2, ignoreCase = true)) return 1 to (start + 2)
        return null
    }

    private fun parseOffsetToken(source: String, start: Int, letter: Char): Pair<Long, Int>? {
        if (start >= source.length) return null
        if (source[start] == 'Z') return 0L to (start + 1)
        if (source.regionMatches(start, "GMT", 0, 3, ignoreCase = true)) {
            val afterGmt = start + 3
            if (afterGmt < source.length && (source[afterGmt] == '+' || source[afterGmt] == '-')) {
                return parseSignedIsoOffset(source, afterGmt)
            }
            return 0L to afterGmt
        }
        if (source[start] == '+' || source[start] == '-') {
            return parseSignedIsoOffset(source, start)
        }
        return null
    }

    private fun parseSignedIsoOffset(source: String, start: Int): Pair<Long, Int>? {
        val sign = if (source[start] == '+') 1L else -1L
        var i = start + 1
        if (i + 2 > source.length || source[i] !in '0'..'9' || source[i + 1] !in '0'..'9') return null
        val hh = source.substring(i, i + 2).toLong()
        i += 2
        var mm = 0L
        if (i < source.length && source[i] == ':') {
            if (i + 3 <= source.length && source[i + 1] in '0'..'9' && source[i + 2] in '0'..'9') {
                mm = source.substring(i + 1, i + 3).toLong()
                i += 3
            }
        } else if (i + 2 <= source.length && source[i] in '0'..'9' && source[i + 1] in '0'..'9') {
            mm = source.substring(i, i + 2).toLong()
            i += 2
        }
        if (!isLenient && (hh > 23 || mm > 59)) return null
        return (sign * (hh * 3600L + mm * 60L) * 1000L) to i
    }

    private fun parseZoneNameToken(source: String, start: Int): Pair<Long, Int>? {
        if (start >= source.length) return null
        if (source.regionMatches(start, "GMT", 0, 3, ignoreCase = true) ||
            source.regionMatches(start, "UTC", 0, 3, ignoreCase = true)
        ) {
            val after = start + 3
            if (after < source.length && (source[after] == '+' || source[after] == '-')) {
                return parseSignedIsoOffset(source, after)
            }
            return 0L to after
        }
        if (source[start] == '+' || source[start] == '-') {
            return parseSignedIsoOffset(source, start)
        }
        var i = start
        while (i < source.length) {
            val c = source[i]
            if (c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c == '/' || c == '_' || c == '-' || c == '+') {
                i++
            } else {
                break
            }
        }
        if (i == start) return null
        val tzName = source.substring(start, i)
        val offsetMillis = when (tzName.uppercase()) {
            "Z", "UT" -> 0L
            "EST" -> -5 * 3600_000L
            "EDT" -> -4 * 3600_000L
            "CST" -> -6 * 3600_000L
            "CDT" -> -5 * 3600_000L
            "MST" -> -7 * 3600_000L
            "MDT" -> -6 * 3600_000L
            "PST" -> -8 * 3600_000L
            "PDT" -> -7 * 3600_000L
            "BST" -> 1 * 3600_000L
            "CET" -> 1 * 3600_000L
            "CEST" -> 2 * 3600_000L
            else -> TimeZone.getTimeZone(tzName).rawOffset.toLong()
        }
        return offsetMillis to i
    }

    private fun isGregorianLeap(year: Int): Boolean {
        return (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0)
    }

    private fun daysInMonth(year: Int, month1Based: Int): Int = when (month1Based) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isGregorianLeap(year)) 29 else 28
        else -> 31
    }

    private fun computeDayOfYear(year: Int, month1Based: Int, day: Int): Int {
        var total = day
        for (m in 1 until month1Based) {
            total += daysInMonth(year, m)
        }
        return total
    }

    private fun computeLocalEpochMillis(
        year: Int,
        month0Based: Int,
        day: Int,
        hour: Int,
        min: Int,
        sec: Int,
        millis: Int
    ): Long {
        val totalMonths = year.toLong() * 12L + month0Based.toLong()
        var y = totalMonths.floorDiv(12L)
        var m = totalMonths.mod(12L) + 1L
        if (m <= 2L) {
            y -= 1
            m += 12
        }
        val era = (if (y >= 0) y else y - 399L) / 400L
        val yoe = y - era * 400L
        val doy = (153 * (m - 3) + 2) / 5 + day - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        val epochDays = era * 146097L + doe - 719468L
        val dayMillis = (hour * 3600L + min * 60L + sec) * 1000L + millis
        return epochDays * 86400000L + dayMillis
    }
}
