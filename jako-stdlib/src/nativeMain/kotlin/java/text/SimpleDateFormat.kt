package java.text

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

actual open class SimpleDateFormat : DateFormat {
    private var patternStr: String
    private var symbols: DateFormatSymbols

    actual constructor() : this("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    actual constructor(pattern: String) : this(pattern, Locale.getDefault())

    actual constructor(pattern: String, locale: Locale) : super() {
        this.patternStr = pattern
        this.symbols = DateFormatSymbols.getInstance(locale)
        compilePattern(pattern)
    }

    actual constructor(pattern: String, formatSymbols: DateFormatSymbols) : super() {
        this.patternStr = pattern
        this.symbols = formatSymbols.clone() as DateFormatSymbols
        compilePattern(pattern)
    }

    actual open fun toPattern(): String = patternStr

    actual open fun applyPattern(pattern: String) {
        this.patternStr = pattern
        compilePattern(pattern)
    }

    actual open fun getDateFormatSymbols(): DateFormatSymbols =
        symbols.clone() as DateFormatSymbols

    actual open fun setDateFormatSymbols(newFormatSymbols: DateFormatSymbols) {
        this.symbols = newFormatSymbols.clone() as DateFormatSymbols
    }

    private enum class DateField {
        YEAR, YEAR_2DIGIT, MONTH_NUM, MONTH_SHORT, MONTH_LONG, DAY, HOUR, MINUTE, SECOND, MILLIS, TZ_OFFSET, TZ_NAME, SKIP
    }

    private var compiledRegex: Regex? = null
    private var compiledFields: List<DateField> = emptyList()

    private fun compilePattern(pattern: String) {
        val fields = mutableListOf<DateField>()
        val regexPattern = StringBuilder("^")
        var i = 0
        var valid = true
        while (i < pattern.length) {
            val c = pattern[i]
            when (c) {
                '\'' -> {
                    val endQuote = pattern.indexOf('\'', i + 1)
                    if (endQuote != -1) {
                        val literal = pattern.substring(i + 1, endQuote)
                        regexPattern.append(Regex.escape(literal))
                        i = endQuote + 1
                    } else {
                        regexPattern.append(Regex.escape("'"))
                        i++
                    }
                }
                'y' -> {
                    var count = 0
                    while (i < pattern.length && pattern[i] == 'y') {
                        count++
                        i++
                    }
                    if (count >= 4) {
                        regexPattern.append("(\\d{4})")
                        fields.add(DateField.YEAR)
                    } else {
                        regexPattern.append("(\\d{2})")
                        fields.add(DateField.YEAR_2DIGIT)
                    }
                }
                'M' -> {
                    var count = 0
                    while (i < pattern.length && pattern[i] == 'M') {
                        count++
                        i++
                    }
                    when {
                        count >= 4 -> {
                            regexPattern.append("([^\\s,/-]+)")
                            fields.add(DateField.MONTH_LONG)
                        }
                        count == 3 -> {
                            regexPattern.append("([^\\s,/-]+)")
                            fields.add(DateField.MONTH_SHORT)
                        }
                        else -> {
                            regexPattern.append("(\\d{1,2})")
                            fields.add(DateField.MONTH_NUM)
                        }
                    }
                }
                'd' -> {
                    while (i < pattern.length && pattern[i] == 'd') i++
                    regexPattern.append("(\\d{1,2})")
                    fields.add(DateField.DAY)
                }
                'E' -> {
                    while (i < pattern.length && pattern[i] == 'E') i++
                    regexPattern.append("([^\\s,]+)")
                    fields.add(DateField.SKIP)
                }
                'H', 'h', 'K', 'k' -> {
                    while (i < pattern.length && (pattern[i] == 'H' || pattern[i] == 'h' || pattern[i] == 'K' || pattern[i] == 'k')) i++
                    regexPattern.append("(\\d{1,2})")
                    fields.add(DateField.HOUR)
                }
                'm' -> {
                    while (i < pattern.length && pattern[i] == 'm') i++
                    regexPattern.append("(\\d{1,2})")
                    fields.add(DateField.MINUTE)
                }
                's' -> {
                    while (i < pattern.length && pattern[i] == 's') i++
                    regexPattern.append("(\\d{1,2})")
                    fields.add(DateField.SECOND)
                }
                'S' -> {
                    while (i < pattern.length && pattern[i] == 'S') i++
                    regexPattern.append("(\\d{1,3})")
                    fields.add(DateField.MILLIS)
                }
                'Z' -> {
                    while (i < pattern.length && pattern[i] == 'Z') i++
                    regexPattern.append("([+-]\\d{4}|Z)")
                    fields.add(DateField.TZ_OFFSET)
                }
                'z' -> {
                    while (i < pattern.length && pattern[i] == 'z') i++
                    regexPattern.append("([A-Za-z0-9+-]+)")
                    fields.add(DateField.TZ_NAME)
                }
                else -> {
                    regexPattern.append(Regex.escape(c.toString()))
                    i++
                }
            }
        }
        if (valid && fields.isNotEmpty()) {
            compiledRegex = Regex(regexPattern.toString())
            compiledFields = fields
        } else {
            compiledRegex = null
            compiledFields = emptyList()
        }
    }

    actual override fun format(date: Date): String {
        val cal = GregorianCalendar(timeZone)
        cal.time = date

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)
        val millis = cal.get(Calendar.MILLISECOND)

        val offsetMillis = timeZone.getOffset(date.time)
        val offsetMinutes = offsetMillis / 60000
        val sign = if (offsetMinutes >= 0) "+" else "-"
        val absMinutes = kotlin.math.abs(offsetMinutes)
        val tzRfc822 = "$sign${(absMinutes / 60).toString().padStart(2, '0')}${(absMinutes % 60).toString().padStart(2, '0')}"

        val monthsLong = symbols.getMonths()
        val monthsShort = symbols.getShortMonths()
        val daysLong = symbols.getWeekdays()
        val daysShort = symbols.getShortWeekdays()
        val amPm = symbols.getAmPmStrings()

        val sb = StringBuilder()
        var i = 0
        while (i < patternStr.length) {
            val c = patternStr[i]
            if (c == '\'') {
                val endQuote = patternStr.indexOf('\'', i + 1)
                if (endQuote != -1) {
                    if (endQuote == i + 1) {
                        sb.append('\'')
                    } else {
                        sb.append(patternStr.substring(i + 1, endQuote))
                    }
                    i = endQuote + 1
                } else {
                    sb.append('\'')
                    i++
                }
                continue
            }
            var count = 0
            while (i < patternStr.length && patternStr[i] == c) {
                count++
                i++
            }
            when (c) {
                'y' -> {
                    if (count == 2) {
                        sb.append((year % 100).toString().padStart(2, '0'))
                    } else {
                        sb.append(year.toString().padStart(count, '0'))
                    }
                }
                'M' -> {
                    when {
                        count >= 4 -> sb.append(monthsLong.getOrElse(month) { "" })
                        count == 3 -> sb.append(monthsShort.getOrElse(month) { "" })
                        else -> sb.append((month + 1).toString().padStart(count, '0'))
                    }
                }
                'd' -> sb.append(day.toString().padStart(count, '0'))
                'E' -> {
                    if (count >= 4) {
                        sb.append(daysLong.getOrElse(dayOfWeek) { "" })
                    } else {
                        sb.append(daysShort.getOrElse(dayOfWeek) { "" })
                    }
                }
                'a' -> sb.append(amPm.getOrElse(if (hour < 12) 0 else 1) { "" })
                'H' -> sb.append(hour.toString().padStart(count, '0'))
                'h' -> {
                    val h12 = if (hour % 12 == 0) 12 else hour % 12
                    sb.append(h12.toString().padStart(count, '0'))
                }
                'm' -> sb.append(minute.toString().padStart(count, '0'))
                's' -> sb.append(second.toString().padStart(count, '0'))
                'S' -> sb.append(millis.toString().padStart(count, '0'))
                'Z' -> sb.append(tzRfc822)
                'z' -> sb.append(timeZone.getID())
                else -> repeat(count) { sb.append(c) }
            }
        }
        return sb.toString()
    }

    /**
     * [Nullability Decision] parse returns Date? (null when date cannot be parsed at pos).
     */
    actual override fun parse(source: String, pos: ParsePosition): Date? {
        val dateString = source.substring(pos.getIndex()).trim()

        val regex = compiledRegex
        if (regex != null) {
            val match = regex.find(dateString)
            if (match != null) {
                var year = 1970
                var month = 0
                var day = 1
                var hour = 0
                var min = 0
                var sec = 0
                var millis = 0
                var tzOffsetMillis: Long? = null

                for (idx in compiledFields.indices) {
                    val groupVal = match.groupValues[idx + 1]
                    when (compiledFields[idx]) {
                        DateField.YEAR -> year = groupVal.toIntOrNull() ?: 1970
                        DateField.YEAR_2DIGIT -> {
                            val y = groupVal.toIntOrNull() ?: 0
                            year = if (y < 70) 2000 + y else 1900 + y
                        }
                        DateField.MONTH_NUM -> month = (groupVal.toIntOrNull() ?: 1) - 1
                        DateField.MONTH_SHORT, DateField.MONTH_LONG -> month = parseMonth(groupVal)
                        DateField.DAY -> day = groupVal.toIntOrNull() ?: 1
                        DateField.HOUR -> hour = groupVal.toIntOrNull() ?: 0
                        DateField.MINUTE -> min = groupVal.toIntOrNull() ?: 0
                        DateField.SECOND -> sec = groupVal.toIntOrNull() ?: 0
                        DateField.MILLIS -> millis = groupVal.padEnd(3, '0').take(3).toIntOrNull() ?: 0
                        DateField.TZ_OFFSET -> tzOffsetMillis = parseTzOffset(groupVal)
                        DateField.TZ_NAME -> tzOffsetMillis = parseTzName(groupVal)
                        DateField.SKIP -> {}
                    }
                }

                val offset = tzOffsetMillis ?: timeZone.rawOffset.toLong()
                pos.setIndex(pos.getIndex() + match.value.length)
                return Date(computeEpochMillis(year, month, day, hour, min, sec, millis, offset))
            }
        }

        // Fallback to HTTP date formats
        val rfc1123 = Regex("""^([A-Za-z]{3}),\s+(\d{1,2})\s+([A-Za-z]{3})\s+(\d{4})\s+(\d{2}):(\d{2}):(\d{2})\s+([A-Za-z0-9+-]+)""")
        val rfc850 = Regex("""^([A-Za-z]+),\s+(\d{1,2})-([A-Za-z]{3})-(\d{2})\s+(\d{2}):(\d{2}):(\d{2})\s+([A-Za-z0-9+-]+)""")
        val asctime = Regex("""^([A-Za-z]{3})\s+([A-Za-z]{3})\s+(\d{1,2})\s+(\d{2}):(\d{2}):(\d{2})\s+(\d{4})""")

        var match = rfc1123.find(dateString)
        if (match != null) {
            val (_, dayStr, monthStr, yearStr, hourStr, minStr, secStr, tzStr) = match.destructured
            val day = dayStr.toInt()
            val month = parseMonth(monthStr)
            val year = yearStr.toInt()
            val hour = hourStr.toInt()
            val min = minStr.toInt()
            val sec = secStr.toInt()
            val offset = parseTzName(tzStr) ?: timeZone.rawOffset.toLong()
            pos.setIndex(source.length)
            return Date(computeEpochMillis(year, month, day, hour, min, sec, 0, offset))
        }

        match = rfc850.find(dateString)
        if (match != null) {
            val (_, dayStr, monthStr, yearStr, hourStr, minStr, secStr, tzStr) = match.destructured
            val day = dayStr.toInt()
            val month = parseMonth(monthStr)
            var year = yearStr.toInt()
            year += if (year < 70) 2000 else 1900
            val hour = hourStr.toInt()
            val min = minStr.toInt()
            val sec = secStr.toInt()
            val offset = parseTzName(tzStr) ?: timeZone.rawOffset.toLong()
            pos.setIndex(source.length)
            return Date(computeEpochMillis(year, month, day, hour, min, sec, 0, offset))
        }

        match = asctime.find(dateString)
        if (match != null) {
            val (_, monthStr, dayStr, hourStr, minStr, secStr, yearStr) = match.destructured
            val day = dayStr.trim().toInt()
            val month = parseMonth(monthStr)
            val year = yearStr.toInt()
            val hour = hourStr.toInt()
            val min = minStr.toInt()
            val sec = secStr.toInt()
            pos.setIndex(source.length)
            return Date(computeEpochMillis(year, month, day, hour, min, sec, 0, 0L))
        }

        pos.errorIndex = pos.getIndex()
        return null
    }

    private fun parseMonth(monthStr: String): Int {
        val monthsShort = symbols.getShortMonths()
        val monthsLong = symbols.getMonths()
        val idxShort = monthsShort.indexOfFirst { it.isNotEmpty() && it.equals(monthStr, ignoreCase = true) }
        if (idxShort >= 0) return idxShort
        val idxLong = monthsLong.indexOfFirst { it.isNotEmpty() && it.equals(monthStr, ignoreCase = true) }
        return if (idxLong >= 0) idxLong else 0
    }

    private fun parseTzOffset(tzStr: String): Long {
        if (tzStr == "Z") return 0L
        if (tzStr.length >= 5 && (tzStr.startsWith("+") || tzStr.startsWith("-"))) {
            val sign = if (tzStr[0] == '+') 1L else -1L
            val hours = tzStr.substring(1, 3).toLongOrNull() ?: 0L
            val mins = tzStr.substring(3, 5).toLongOrNull() ?: 0L
            return sign * (hours * 3600L + mins * 60L) * 1000L
        }
        return 0L
    }

    private fun parseTzName(tzStr: String): Long? {
        if (tzStr.equals("GMT", ignoreCase = true) || tzStr.equals("UTC", ignoreCase = true)) return 0L
        if (tzStr.startsWith("+") || tzStr.startsWith("-")) return parseTzOffset(tzStr)
        val tz = TimeZone.getTimeZone(tzStr)
        return tz.rawOffset.toLong()
    }

    private fun computeEpochMillis(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        min: Int,
        sec: Int,
        millis: Int,
        tzOffsetMillis: Long
    ): Long {
        var y = year.toLong()
        var m = month.toLong() + 1
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val era = (if (y >= 0) y else y - 399L) / 400L
        val yoe = y - era * 400L
        val doy = (153 * (m - 3) + 2) / 5 + day - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        val epochDays = era * 146097L + doe - 719468L
        val dayMillis = (hour * 3600L + min * 60L + sec) * 1000L + millis
        return epochDays * 86400000L + dayMillis - tzOffsetMillis
    }
}
