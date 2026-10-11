package java.text

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

actual open class SimpleDateFormat actual constructor(
    actual val pattern: String,
    actual val locale: Locale
) : DateFormat() {
    actual constructor(pattern: String) : this(pattern, Locale.getDefault())

    private val monthsShort = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val monthsLong = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    private val daysShort = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    private val daysLong = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    private enum class DateField {
        YEAR, YEAR_2DIGIT, MONTH_NUM, MONTH_SHORT, MONTH_LONG, DAY, HOUR, MINUTE, SECOND, MILLIS, TZ_OFFSET, TZ_NAME, SKIP
    }

    private val compiledRegex: Regex?
    private val compiledFields: List<DateField>

    init {
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
                            regexPattern.append("([A-Za-z]+)")
                            fields.add(DateField.MONTH_LONG)
                        }
                        count == 3 -> {
                            regexPattern.append("([A-Za-z]{3})")
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
                    regexPattern.append("([A-Za-z]+)")
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

        var result = pattern
        result = result.replace("yyyy", year.toString().padStart(4, '0'))
        result = result.replace("yy", (year % 100).toString().padStart(2, '0'))
        result = result.replace("MMMM", monthsLong.getOrElse(month) { "" })
        result = result.replace("MMM", monthsShort.getOrElse(month) { "" })
        result = result.replace("MM", (month + 1).toString().padStart(2, '0'))
        result = result.replace("dd", day.toString().padStart(2, '0'))
        result = result.replace("EEEE", daysLong.getOrElse(dayOfWeek - 1) { "" })
        result = result.replace("EEE", daysShort.getOrElse(dayOfWeek - 1) { "" })
        result = result.replace("HH", hour.toString().padStart(2, '0'))
        result = result.replace("mm", minute.toString().padStart(2, '0'))
        result = result.replace("ss", second.toString().padStart(2, '0'))
        result = result.replace("SSS", millis.toString().padStart(3, '0'))
        result = result.replace("Z", tzRfc822)
        result = result.replace("z", timeZone.getID())
        result = result.replace(Regex("'(.*?)'")) { it.groupValues[1] }
        return result
    }

    actual override fun parse(source: String, pos: ParsePosition): Date? {
        val dateString = source.substring(pos.getIndex()).trim()

        if (compiledRegex != null) {
            val match = compiledRegex.find(dateString)
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
        val idxShort = monthsShort.indexOfFirst { it.equals(monthStr, ignoreCase = true) }
        if (idxShort >= 0) return idxShort
        val idxLong = monthsLong.indexOfFirst { it.equals(monthStr, ignoreCase = true) }
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
