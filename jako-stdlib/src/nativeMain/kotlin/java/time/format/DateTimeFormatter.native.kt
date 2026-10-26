package java.time.format

import java.time.DateTimeException
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.MonthDay
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.formatIsoDate
import java.time.formatIsoInstantFraction
import java.time.formatIsoTime
import java.time.parseIsoLocalDateString
import java.time.parseIsoLocalDateTimeString
import java.time.parseIsoLocalTimeString
import java.time.parseOffsetDateTime
import java.text.DateFormatSymbols
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAccessor
import java.util.Locale

private const val KIND_ISO_DATE = 1
private const val KIND_ISO_TIME = 2
private const val KIND_ISO_DATE_TIME = 3
private const val KIND_ISO_INSTANT = 4
private const val KIND_PATTERN = 5
private const val KIND_ISO_OFFSET_DATE_TIME = 6
private const val KIND_ISO_ZONED_DATE_TIME = 7
private const val KIND_BASIC_ISO_DATE = 8

private sealed class PatternToken
private data class FieldToken(val letter: Char, val count: Int) : PatternToken()
private data class LiteralToken(val text: String) : PatternToken()
private data class OptionalToken(val tokens: List<PatternToken>) : PatternToken()

private val SHORT_MONTHS_EN = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

private val FULL_MONTHS_EN = arrayOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private val NARROW_MONTHS_EN = arrayOf(
    "J", "F", "M", "A", "M", "J",
    "J", "A", "S", "O", "N", "D"
)

private val SHORT_WEEKDAYS_EN = arrayOf(
    "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
)

private val FULL_WEEKDAYS_EN = arrayOf(
    "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
)

private val NARROW_WEEKDAYS_EN = arrayOf(
    "M", "T", "W", "T", "F", "S", "S"
)

private data class ParseState(
    var era: Int? = null,
    var year: Int? = null,
    var month: Int? = null,
    var dayOfMonth: Int? = null,
    var dayOfYear: Int? = null,
    var dayOfWeek: Int? = null,
    var amPm: Int? = null,
    var hour24: Int? = null,
    var clockHour24: Int? = null,
    var hour12: Int? = null,
    var clockHour12: Int? = null,
    var minute: Int? = null,
    var second: Int? = null,
    var nano: Int? = null,
    var offset: ZoneOffset? = null,
    var zone: ZoneId? = null
)

actual class DateTimeFormatter private constructor(
    private val kind: Int,
    private val tokens: List<PatternToken> = emptyList(),
    actual val locale: Locale = Locale.getDefault(),
    actual val zone: ZoneId? = null
) {
    actual fun getLocale(): Locale = locale
    actual fun getZone(): ZoneId? = zone

    actual fun withLocale(locale: Locale): DateTimeFormatter {
        if (this.locale == locale) return this
        return DateTimeFormatter(kind, tokens, locale, zone)
    }

    actual fun withZone(zone: ZoneId?): DateTimeFormatter {
        if (this.zone == zone) return this
        return DateTimeFormatter(kind, tokens, locale, zone)
    }

    actual fun format(temporal: TemporalAccessor): String {
        val effectiveTemporal = applyZoneOverride(temporal)
        return when (kind) {
            KIND_ISO_DATE -> formatIsoDate(yearOf(effectiveTemporal), monthOf(effectiveTemporal), dayOf(effectiveTemporal))
            KIND_ISO_TIME -> formatIsoTime(hourOf(effectiveTemporal), minuteOf(effectiveTemporal), secondOf(effectiveTemporal), nanoOf(effectiveTemporal), true)
            KIND_ISO_DATE_TIME -> formatIsoDate(yearOf(effectiveTemporal), monthOf(effectiveTemporal), dayOf(effectiveTemporal)) +
                "T" + formatIsoTime(hourOf(effectiveTemporal), minuteOf(effectiveTemporal), secondOf(effectiveTemporal), nanoOf(effectiveTemporal), true)
            KIND_ISO_OFFSET_DATE_TIME -> {
                val base = formatIsoDate(yearOf(effectiveTemporal), monthOf(effectiveTemporal), dayOf(effectiveTemporal)) +
                    "T" + formatIsoTime(hourOf(effectiveTemporal), minuteOf(effectiveTemporal), secondOf(effectiveTemporal), nanoOf(effectiveTemporal), true)
                base + offsetOf(effectiveTemporal).toString()
            }
            KIND_ISO_ZONED_DATE_TIME -> {
                val base = formatIsoDate(yearOf(effectiveTemporal), monthOf(effectiveTemporal), dayOf(effectiveTemporal)) +
                    "T" + formatIsoTime(hourOf(effectiveTemporal), minuteOf(effectiveTemporal), secondOf(effectiveTemporal), nanoOf(effectiveTemporal), true)
                val off = offsetOf(effectiveTemporal)
                val z = zoneIdOfOrNull(effectiveTemporal)
                if (z != null && z !== off && z.id != off.id) {
                    "$base$off[$z]"
                } else {
                    "$base$off"
                }
            }
            KIND_BASIC_ISO_DATE -> {
                val y = yearOf(effectiveTemporal)
                val m = monthOf(effectiveTemporal)
                val d = dayOf(effectiveTemporal)
                val yStr = if (y < 0) "-" + kotlin.math.abs(y).toString().padStart(4, '0') else y.toString().padStart(4, '0')
                val base = yStr + m.toString().padStart(2, '0') + d.toString().padStart(2, '0')
                val off = offsetOfOrNull(effectiveTemporal)
                if (off != null) base + formatOffsetField(off, 'X', 2) else base
            }
            KIND_ISO_INSTANT -> formatInstant(instantOf(temporal))
            KIND_PATTERN -> formatTokens(effectiveTemporal, tokens)
            else -> throw DateTimeException("Unknown formatter")
        }
    }

    private fun applyZoneOverride(temporal: TemporalAccessor): TemporalAccessor {
        val overrideZone = zone ?: return temporal
        return when (temporal) {
            is Instant -> ZonedDateTime.ofInstant(temporal, overrideZone)
            is ZonedDateTime -> temporal.withZoneSameInstant(overrideZone)
            is OffsetDateTime -> ZonedDateTime.ofInstant(temporal.toInstant(), overrideZone)
            else -> temporal
        }
    }

    actual fun parse(text: CharSequence): TemporalAccessor {
        val s = text.toString()
        return try {
            when (kind) {
                KIND_ISO_DATE -> parseIsoLocalDateString(s)
                KIND_ISO_TIME -> parseIsoLocalTimeString(s)
                KIND_ISO_DATE_TIME -> {
                    val ldt = parseIsoLocalDateTimeString(s)
                    if (zone != null) ZonedDateTime.ofLocal(ldt, zone, null) else ldt
                }
                KIND_ISO_OFFSET_DATE_TIME -> {
                    val odt = parseOffsetDateTime(s)
                    if (zone != null) ZonedDateTime.ofInstant(odt.toInstant(), zone) else odt
                }
                KIND_ISO_ZONED_DATE_TIME -> {
                    val zdt = ZonedDateTime.parse(s)
                    if (zone != null && !s.contains('[')) zdt.withZoneSameInstant(zone) else zdt
                }
                KIND_BASIC_ISO_DATE -> parseBasicIsoDate(s)
                KIND_ISO_INSTANT -> parseIsoInstant(s)
                KIND_PATTERN -> parsePattern(s)
                else -> throw DateTimeException("Unknown formatter")
            }
        } catch (ex: DateTimeParseException) {
            throw ex
        } catch (ex: DateTimeException) {
            throw DateTimeParseException("Text '$s' could not be parsed: ${ex.message}", s, 0, ex)
        } catch (ex: RuntimeException) {
            throw DateTimeParseException("Text '$s' could not be parsed", s, 0, ex)
        }
    }

    private fun parseBasicIsoDate(s: String): TemporalAccessor {
        if (s.length < 8) throw DateTimeParseException("Text '$s' could not be parsed", s, 0)
        val y = s.substring(0, 4).toIntOrNull() ?: throw DateTimeParseException("Text '$s' could not be parsed", s, 0)
        val m = s.substring(4, 6).toIntOrNull() ?: throw DateTimeParseException("Text '$s' could not be parsed", s, 4)
        val d = s.substring(6, 8).toIntOrNull() ?: throw DateTimeParseException("Text '$s' could not be parsed", s, 6)
        val date = LocalDate.of(y, m, d)
        if (s.length > 8) {
            val rem = s.substring(8)
            val off = if (rem == "Z") ZoneOffset.UTC else ZoneOffset.of(rem)
            return OffsetDateTime.of(date, LocalTime.MIDNIGHT, off)
        }
        return date
    }

    private fun formatTokens(temporal: TemporalAccessor, tokenList: List<PatternToken>): String {
        val sb = StringBuilder()
        for (token in tokenList) {
            when (token) {
                is LiteralToken -> sb.append(token.text)
                is FieldToken -> sb.append(formatField(temporal, token.letter, token.count))
                is OptionalToken -> {
                    try {
                        sb.append(formatTokens(temporal, token.tokens))
                    } catch (_: DateTimeException) {
                        // Optional section omitted when required fields are absent
                    }
                }
            }
        }
        return sb.toString()
    }

    private fun formatField(temporal: TemporalAccessor, letter: Char, count: Int): String {
        val dfs = DateFormatSymbols.getInstance(locale)
        when (letter) {
            'G' -> {
                val y = yearOf(temporal)
                val eraIdx = if (y >= 1) 1 else 0
                return dfs.getEras().getOrElse(eraIdx) { if (eraIdx == 1) "AD" else "BC" }
            }
            'y', 'u', 'Y' -> {
                val y = yearOf(temporal)
                val value = if (letter == 'y' && y <= 0) 1 - y else y
                if (count == 2) {
                    return (kotlin.math.abs(value) % 100).toString().padStart(2, '0')
                }
                val absStr = kotlin.math.abs(value).toString().padStart(count, '0')
                return when {
                    value < 0 -> "-$absStr"
                    count >= 4 && value > 9999 -> "+$absStr"
                    else -> absStr
                }
            }
            'Q', 'q' -> {
                val q = (monthOf(temporal) - 1) / 3 + 1
                return when (count) {
                    1 -> q.toString()
                    2 -> q.toString().padStart(2, '0')
                    3 -> "Q$q"
                    4 -> when (q) {
                        1 -> "1st quarter"
                        2 -> "2nd quarter"
                        3 -> "3rd quarter"
                        else -> "4th quarter"
                    }
                    else -> q.toString()
                }
            }
            'M', 'L' -> {
                val m = monthOf(temporal)
                return when (count) {
                    1 -> m.toString()
                    2 -> m.toString().padStart(2, '0')
                    3 -> dfs.getShortMonths().getOrElse(m - 1) { SHORT_MONTHS_EN[m - 1] }
                    4 -> dfs.getMonths().getOrElse(m - 1) { FULL_MONTHS_EN[m - 1] }
                    5 -> NARROW_MONTHS_EN[m - 1]
                    else -> throw DateTimeException("Too many pattern letters: $letter")
                }
            }
            'd' -> return dayOf(temporal).toString().padStart(count, '0')
            'D' -> return dayOfYearOf(temporal).toString().padStart(count, '0')
            'E', 'e', 'c' -> {
                val dow = dayOfWeekOf(temporal) // 1 = Mon .. 7 = Sun
                if ((letter == 'e' || letter == 'c') && count <= 2) {
                    return dow.toString().padStart(count, '0')
                }
                val calDow = if (dow == 7) 1 else dow + 1 // 1 = Sun .. 7 = Sat
                return when (count) {
                    1, 2, 3 -> dfs.getShortWeekdays().getOrElse(calDow) { SHORT_WEEKDAYS_EN[dow - 1] }
                    4 -> dfs.getWeekdays().getOrElse(calDow) { FULL_WEEKDAYS_EN[dow - 1] }
                    5 -> NARROW_WEEKDAYS_EN[dow - 1]
                    else -> dfs.getShortWeekdays().getOrElse(calDow) { SHORT_WEEKDAYS_EN[dow - 1] }
                }
            }
            'a' -> {
                val h = hourOf(temporal)
                val amPmIdx = if (h < 12) 0 else 1
                return dfs.getAmPmStrings().getOrElse(amPmIdx) { if (amPmIdx == 0) "AM" else "PM" }
            }
            'H' -> return hourOf(temporal).toString().padStart(count, '0')
            'k' -> {
                val h = hourOf(temporal)
                val v = if (h == 0) 24 else h
                return v.toString().padStart(count, '0')
            }
            'K' -> return (hourOf(temporal) % 12).toString().padStart(count, '0')
            'h' -> {
                val h = hourOf(temporal) % 12
                val v = if (h == 0) 12 else h
                return v.toString().padStart(count, '0')
            }
            'm' -> return minuteOf(temporal).toString().padStart(count, '0')
            's' -> return secondOf(temporal).toString().padStart(count, '0')
            'S' -> {
                val nanoStr = nanoOf(temporal).toString().padStart(9, '0')
                return nanoStr.substring(0, count.coerceIn(1, 9))
            }
            'n' -> return nanoOf(temporal).toString().padStart(count, '0')
            'N' -> {
                val nod = hourOf(temporal) * 3600_000_000_000L +
                    minuteOf(temporal) * 60_000_000_000L +
                    secondOf(temporal) * 1_000_000_000L +
                    nanoOf(temporal)
                return nod.toString().padStart(count, '0')
            }
            'A' -> {
                val mod = (hourOf(temporal) * 3600L + minuteOf(temporal) * 60L + secondOf(temporal)) * 1000L +
                    nanoOf(temporal) / 1_000_000L
                return mod.toString().padStart(count, '0')
            }
            'V' -> return zoneIdOf(temporal).id
            'z' -> return zoneIdOf(temporal).id
            'X', 'x' -> return formatOffsetField(offsetOf(temporal), letter, count)
            'Z' -> return formatRfcOffset(offsetOf(temporal), count)
            'O' -> return formatLocalizedOffset(offsetOf(temporal), count)
            else -> throw DateTimeException("Unknown pattern letter: $letter")
        }
    }

    private fun formatOffsetField(offset: ZoneOffset, letter: Char, count: Int): String {
        val total = offset.totalSeconds
        if (total == 0 && letter == 'X') return "Z"
        val sign = if (total < 0) "-" else "+"
        val absTotal = kotlin.math.abs(total)
        val hours = (absTotal / 3600).toString().padStart(2, '0')
        val minutes = ((absTotal % 3600) / 60).toString().padStart(2, '0')
        val seconds = (absTotal % 60).toString().padStart(2, '0')
        return when (count) {
            1 -> if (minutes == "00" && seconds == "00") "$sign$hours" else "$sign$hours$minutes"
            2 -> "$sign$hours$minutes"
            3 -> "$sign$hours:$minutes"
            4 -> if (seconds == "00") "$sign$hours$minutes" else "$sign$hours$minutes$seconds"
            5 -> if (seconds == "00") "$sign$hours:$minutes" else "$sign$hours:$minutes:$seconds"
            else -> throw DateTimeException("Too many pattern letters: $letter")
        }
    }

    private fun formatRfcOffset(offset: ZoneOffset, count: Int): String {
        return when (count) {
            1, 2, 3 -> formatOffsetField(offset, 'x', if (offset.totalSeconds % 60 == 0) 2 else 4)
            4 -> formatLocalizedOffset(offset, 4)
            5 -> formatOffsetField(offset, 'X', 5)
            else -> throw DateTimeException("Too many pattern letters: Z")
        }
    }

    private fun formatLocalizedOffset(offset: ZoneOffset, count: Int): String {
        val total = offset.totalSeconds
        if (total == 0) return "GMT"
        val sign = if (total < 0) "-" else "+"
        val absTotal = kotlin.math.abs(total)
        val hours = absTotal / 3600
        val minutes = (absTotal % 3600) / 60
        val seconds = absTotal % 60
        return if (count == 1) {
            if (seconds != 0) {
                "GMT$sign$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
            } else if (minutes != 0) {
                "GMT$sign$hours:${minutes.toString().padStart(2, '0')}"
            } else {
                "GMT$sign$hours"
            }
        } else {
            val hStr = hours.toString().padStart(2, '0')
            val mStr = minutes.toString().padStart(2, '0')
            if (seconds != 0) {
                "GMT$sign$hStr:$mStr:${seconds.toString().padStart(2, '0')}"
            } else {
                "GMT$sign$hStr:$mStr"
            }
        }
    }

    private fun parsePattern(text: String): TemporalAccessor {
        val state = ParseState()
        val endPos = parseTokenSequence(text, 0, tokens, state)
        if (endPos != text.length) {
            throw DateTimeParseException("Text '$text' could not be parsed, unparsed text found at index $endPos", text, endPos)
        }
        return resolveParsedTemporal(text, state)
    }

    private fun parseTokenSequence(
        text: String,
        startPos: Int,
        tokenList: List<PatternToken>,
        state: ParseState
    ): Int {
        var pos = startPos
        for (idx in tokenList.indices) {
            val token = tokenList[idx]
            when (token) {
                is LiteralToken -> {
                    val lit = token.text
                    if (pos + lit.length > text.length || text.substring(pos, pos + lit.length) != lit) {
                        throw DateTimeParseException("Text '$text' could not be parsed at index $pos", text, pos)
                    }
                    pos += lit.length
                }
                is OptionalToken -> {
                    val copy = state.copy()
                    try {
                        val nextPos = parseTokenSequence(text, pos, token.tokens, copy)
                        pos = nextPos
                        copyState(copy, state)
                    } catch (_: DateTimeParseException) {
                        // Optional section didn't match; continue from pos with original state
                    }
                }
                is FieldToken -> {
                    val subsequentMinDigits = computeSubsequentMinDigits(tokenList, idx + 1)
                    pos = parseFieldIntoState(text, pos, token.letter, token.count, subsequentMinDigits, state)
                }
            }
        }
        return pos
    }

    private fun copyState(from: ParseState, to: ParseState) {
        to.era = from.era
        to.year = from.year
        to.month = from.month
        to.dayOfMonth = from.dayOfMonth
        to.dayOfYear = from.dayOfYear
        to.dayOfWeek = from.dayOfWeek
        to.amPm = from.amPm
        to.hour24 = from.hour24
        to.clockHour24 = from.clockHour24
        to.hour12 = from.hour12
        to.clockHour12 = from.clockHour12
        to.minute = from.minute
        to.second = from.second
        to.nano = from.nano
        to.offset = from.offset
        to.zone = from.zone
    }

    private fun isNumericToken(token: FieldToken): Boolean {
        return when (token.letter) {
            'y', 'u', 'Y', 'd', 'D', 'H', 'k', 'K', 'h', 'm', 's', 'S', 'n', 'N', 'A' -> true
            'M', 'L', 'Q', 'q', 'e', 'c' -> token.count <= 2
            else -> false
        }
    }

    private fun minDigitsForToken(token: FieldToken): Int {
        return when (token.letter) {
            'S' -> token.count
            'y', 'u', 'Y' -> if (token.count == 2) 2 else if (token.count >= 4) token.count else 1
            else -> token.count
        }
    }

    private fun computeSubsequentMinDigits(tokenList: List<PatternToken>, startIdx: Int): Int {
        var total = 0
        var i = startIdx
        while (i < tokenList.size) {
            val t = tokenList[i]
            if (t is FieldToken && isNumericToken(t)) {
                total += minDigitsForToken(t)
                i++
            } else {
                break
            }
        }
        return total
    }

    private fun parseFieldIntoState(
        text: String,
        start: Int,
        letter: Char,
        count: Int,
        subsequentMinDigits: Int,
        state: ParseState
    ): Int {
        val dfs = DateFormatSymbols.getInstance(locale)
        when (letter) {
            'G' -> {
                val eras = dfs.getEras()
                for (eIdx in eras.indices) {
                    val eStr = eras[eIdx]
                    if (eStr.isNotEmpty() && text.regionMatches(start, eStr, 0, eStr.length, ignoreCase = true)) {
                        state.era = eIdx
                        return start + eStr.length
                    }
                }
                return when {
                    text.regionMatches(start, "AD", 0, 2, ignoreCase = true) -> {
                        state.era = 1
                        start + 2
                    }
                    text.regionMatches(start, "CE", 0, 2, ignoreCase = true) -> {
                        state.era = 1
                        start + 2
                    }
                    text.regionMatches(start, "BCE", 0, 3, ignoreCase = true) -> {
                        state.era = 0
                        start + 3
                    }
                    text.regionMatches(start, "BC", 0, 2, ignoreCase = true) -> {
                        state.era = 0
                        start + 2
                    }
                    else -> throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
                }
            }
            'M', 'L' -> {
                if (count == 3 || count == 4) {
                    val primaryArr = if (count == 4) dfs.getMonths() else dfs.getShortMonths()
                    val fallbackArr = if (count == 4) FULL_MONTHS_EN else SHORT_MONTHS_EN
                    var bestMonth = -1
                    var bestLen = 0
                    for (arr in listOf(primaryArr, fallbackArr)) {
                        for (idx in 0 until minOf(12, arr.size)) {
                            val name = arr[idx]
                            if (name.isNotEmpty() && name.length > bestLen &&
                                text.regionMatches(start, name, 0, name.length, ignoreCase = true)
                            ) {
                                bestMonth = idx + 1
                                bestLen = name.length
                            }
                        }
                    }
                    if (bestMonth > 0) {
                        state.month = bestMonth
                        return start + bestLen
                    }
                    throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
                } else if (count == 5) {
                    throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
                }
                val (v, next) = parseNumericField(text, start, letter, count, subsequentMinDigits)
                state.month = v
                return next
            }
            'E', 'e', 'c' -> {
                if ((letter == 'e' || letter == 'c') && count <= 2) {
                    val (v, next) = parseNumericField(text, start, letter, count, subsequentMinDigits)
                    if (v !in 1..7) throw DateTimeParseException("Invalid day of week: $v", text, start)
                    state.dayOfWeek = v
                    return next
                }
                val dfsNames = if (count >= 4) dfs.getWeekdays() else dfs.getShortWeekdays()
                var bestIsoDow = -1
                var bestLen = 0
                for (calDow in 1..7) {
                    val name = dfsNames.getOrNull(calDow) ?: ""
                    if (name.isNotEmpty() && name.length > bestLen &&
                        text.regionMatches(start, name, 0, name.length, ignoreCase = true)
                    ) {
                        bestIsoDow = if (calDow == 1) 7 else calDow - 1
                        bestLen = name.length
                    }
                }
                if (bestIsoDow > 0) {
                    state.dayOfWeek = bestIsoDow
                    return start + bestLen
                }
                val names = if (count >= 4) FULL_WEEKDAYS_EN else SHORT_WEEKDAYS_EN
                for (idx in names.indices) {
                    val name = names[idx]
                    if (text.regionMatches(start, name, 0, name.length, ignoreCase = true)) {
                        state.dayOfWeek = idx + 1
                        return start + name.length
                    }
                }
                throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
            }
            'a' -> {
                val ampms = dfs.getAmPmStrings()
                for (idx in ampms.indices) {
                    val s = ampms[idx]
                    if (s.isNotEmpty() && text.regionMatches(start, s, 0, s.length, ignoreCase = true)) {
                        state.amPm = idx
                        return start + s.length
                    }
                }
                return when {
                    text.regionMatches(start, "AM", 0, 2, ignoreCase = true) -> {
                        state.amPm = 0
                        start + 2
                    }
                    text.regionMatches(start, "PM", 0, 2, ignoreCase = true) -> {
                        state.amPm = 1
                        start + 2
                    }
                    else -> throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
                }
            }
            'S' -> {
                if (start + count > text.length) {
                    throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
                }
                for (k in 0 until count) {
                    if (text[start + k] !in '0'..'9') {
                        throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
                    }
                }
                val fracDigits = text.substring(start, start + count).padEnd(9, '0')
                state.nano = fracDigits.toInt()
                return start + count
            }
            'X', 'x', 'Z', 'O' -> {
                val (off, next) = parseOffsetAt(text, start, letter, count)
                state.offset = off
                return next
            }
            'V', 'z' -> {
                val (zid, next) = parseZoneIdAt(text, start)
                state.zone = zid
                return next
            }
            else -> {
                val (v, next) = parseNumericField(text, start, letter, count, subsequentMinDigits)
                when (letter) {
                    'y', 'u', 'Y' -> state.year = v
                    'd' -> state.dayOfMonth = v
                    'D' -> state.dayOfYear = v
                    'H' -> state.hour24 = v
                    'k' -> state.clockHour24 = v
                    'K' -> state.hour12 = v
                    'h' -> state.clockHour12 = v
                    'm' -> state.minute = v
                    's' -> state.second = v
                    'n' -> state.nano = v
                }
                return next
            }
        }
    }

    private fun parseNumericField(
        text: String,
        start: Int,
        letter: Char,
        count: Int,
        subsequentMinDigits: Int
    ): Pair<Int, Int> {
        if (start >= text.length) {
            throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
        }
        var i = start
        val allowSign = letter == 'y' || letter == 'u' || letter == 'Y'
        if (allowSign && (text[i] == '+' || text[i] == '-')) {
            if (letter == 'y' && text[i] == '-') {
                throw DateTimeParseException("YearOfEra cannot be negative: '$text'", text, start)
            }
            i++
        }
        val digitStart = i
        var totalConsecutiveDigits = 0
        while (digitStart + totalConsecutiveDigits < text.length && text[digitStart + totalConsecutiveDigits] in '0'..'9') {
            totalConsecutiveDigits++
        }
        val maxDigits = when {
            (letter == 'y' || letter == 'u' || letter == 'Y') && count == 2 -> 2
            (letter == 'y' || letter == 'u' || letter == 'Y') && count >= 4 -> 10
            (letter == 'y' || letter == 'u' || letter == 'Y') -> 10
            letter == 'D' -> if (count >= 2) count else 3
            letter == 'n' -> 9
            count >= 2 -> count
            else -> 2
        }
        val minDigits = when {
            (letter == 'y' || letter == 'u' || letter == 'Y') && count >= 4 -> count
            (letter == 'y' || letter == 'u' || letter == 'Y') && count == 2 -> 2
            count >= 2 -> count
            else -> 1
        }
        val effectiveMaxDigits = if (subsequentMinDigits > 0) {
            (totalConsecutiveDigits - subsequentMinDigits).coerceAtMost(maxDigits)
        } else {
            totalConsecutiveDigits.coerceAtMost(maxDigits)
        }
        if (effectiveMaxDigits < minDigits) {
            throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
        }
        val endIdx = digitStart + effectiveMaxDigits
        val number = text.substring(start, endIdx).toIntOrNull()
            ?: throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
        val value = if ((letter == 'y' || letter == 'u' || letter == 'Y') && count == 2 && number in 0..99 && text[start] != '+' && text[start] != '-') {
            2000 + number
        } else {
            number
        }
        return value to endIdx
    }

    private fun parseOffsetAt(text: String, start: Int, letter: Char, count: Int): Pair<ZoneOffset, Int> {
        if (start >= text.length) {
            throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
        }
        if ((letter == 'X' || (letter == 'Z' && count == 5)) && text[start] == 'Z') {
            return ZoneOffset.UTC to (start + 1)
        }
        if (letter == 'O' || (letter == 'Z' && count == 4)) {
            if (!text.regionMatches(start, "GMT", 0, 3)) {
                throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
            }
            val afterGmt = start + 3
            if (afterGmt >= text.length || (text[afterGmt] != '+' && text[afterGmt] != '-')) {
                return ZoneOffset.UTC to afterGmt
            }
            val (off, endPos) = parseSignedOffset(text, afterGmt)
            return off to endPos
        }
        if (text[start] != '+' && text[start] != '-') {
            throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
        }
        return parseSignedOffset(text, start)
    }

    private fun parseSignedOffset(text: String, start: Int): Pair<ZoneOffset, Int> {
        val sign = text[start]
        var i = start + 1
        val sb = StringBuilder().append(sign)
        // Consume +HH, +HHmm, +HH:mm, +HHmmss, +HH:mm:ss
        var digits = 0
        while (i < text.length && digits < 2 && text[i] in '0'..'9') {
            sb.append(text[i])
            i++
            digits++
        }
        if (digits == 0) {
            throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
        }
        if (i < text.length && text[i] == ':') {
            if (i + 2 < text.length && text[i + 1] in '0'..'9' && text[i + 2] in '0'..'9') {
                sb.append(':').append(text[i + 1]).append(text[i + 2])
                i += 3
                if (i < text.length && text[i] == ':' && i + 2 < text.length && text[i + 1] in '0'..'9' && text[i + 2] in '0'..'9') {
                    sb.append(':').append(text[i + 1]).append(text[i + 2])
                    i += 3
                }
            }
        } else if (i + 1 < text.length && text[i] in '0'..'9' && text[i + 1] in '0'..'9') {
            sb.append(text[i]).append(text[i + 1])
            i += 2
            if (i + 1 < text.length && text[i] in '0'..'9' && text[i + 1] in '0'..'9') {
                sb.append(text[i]).append(text[i + 1])
                i += 2
            }
        }
        val offset = try {
            ZoneOffset.of(sb.toString())
        } catch (ex: DateTimeException) {
            throw DateTimeParseException("Invalid ZoneOffset: ${sb}", text, start, ex)
        }
        return offset to i
    }

    private fun parseZoneIdAt(text: String, start: Int): Pair<ZoneId, Int> {
        if (start >= text.length) {
            throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
        }
        // Match known zone IDs or offset-style zone IDs or standard IANA format Region/City
        var i = start
        if (text[i] == '+' || text[i] == '-') {
            val (off, next) = parseSignedOffset(text, i)
            return off to next
        }
        while (i < text.length) {
            val c = text[i]
            if (c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c == '/' || c == '_' || c == '+' || c == '-' || c == ':') {
                i++
            } else {
                break
            }
        }
        if (i == start) {
            throw DateTimeParseException("Text '$text' could not be parsed at index $start", text, start)
        }
        val raw = text.substring(start, i)
        val zid = try {
            ZoneId.of(raw)
        } catch (ex: DateTimeException) {
            throw DateTimeParseException("Unknown time-zone ID: $raw", text, start, ex)
        }
        return zid to i
    }

    private fun resolveParsedTemporal(text: String, state: ParseState): TemporalAccessor {
        var resolvedYear = state.year
        if (resolvedYear != null && state.era == 0 && resolvedYear > 0) {
            resolvedYear = 1 - resolvedYear
        }

        val date: LocalDate? = when {
            resolvedYear != null && state.month != null && state.dayOfMonth != null ->
                LocalDate.of(resolvedYear, state.month!!, state.dayOfMonth!!)
            resolvedYear != null && state.dayOfYear != null ->
                LocalDate.ofYearDay(resolvedYear, state.dayOfYear!!)
            else -> null
        }

        if (date != null && state.dayOfWeek != null) {
            if (date.dayOfWeek.value != state.dayOfWeek) {
                throw DateTimeParseException(
                    "Conflict found: Parsed DayOfWeek ${state.dayOfWeek} does not match ${date.dayOfWeek} for $date",
                    text,
                    0
                )
            }
        }

        val resolvedHour: Int? = when {
            state.hour24 != null -> state.hour24
            state.clockHour24 != null -> {
                val k = state.clockHour24!!
                if (k !in 1..24) throw DateTimeException("Invalid value for ClockHourOfDay (valid values 1 - 24): $k")
                if (k == 24) 0 else k
            }
            state.clockHour12 != null -> {
                val h = state.clockHour12!!
                if (h !in 1..12) throw DateTimeException("Invalid value for ClockHourOfAmPm (valid values 1 - 12): $h")
                val ampm = state.amPm ?: 0
                (h % 12) + ampm * 12
            }
            state.hour12 != null -> {
                val k = state.hour12!!
                if (k !in 0..11) throw DateTimeException("Invalid value for HourOfAmPm (valid values 0 - 11): $k")
                val ampm = state.amPm ?: 0
                k + ampm * 12
            }
            else -> null
        }

        val hasTime = resolvedHour != null || state.minute != null || state.second != null || state.nano != null
        val time: LocalTime? = if (hasTime) {
            LocalTime.of(resolvedHour ?: 0, state.minute ?: 0, state.second ?: 0, state.nano ?: 0)
        } else {
            null
        }

        val effectiveZone = state.zone ?: this.zone
        val effectiveOffset = state.offset

        return when {
            date != null && time != null && state.zone != null ->
                ZonedDateTime.ofLocal(LocalDateTime.of(date, time), state.zone!!, effectiveOffset)
            date != null && time != null && effectiveOffset != null && this.zone != null -> {
                val odt = OffsetDateTime.of(date, time, effectiveOffset)
                ZonedDateTime.ofInstant(odt.toInstant(), this.zone)
            }
            date != null && time != null && effectiveZone != null ->
                ZonedDateTime.ofLocal(LocalDateTime.of(date, time), effectiveZone, effectiveOffset)
            date != null && time != null && effectiveOffset != null ->
                OffsetDateTime.of(date, time, effectiveOffset)
            date != null && time != null ->
                LocalDateTime.of(date, time)
            date != null ->
                date
            time != null && effectiveOffset != null ->
                OffsetTime.of(time, effectiveOffset)
            time != null ->
                time
            resolvedYear != null && state.month != null ->
                YearMonth.of(resolvedYear, state.month!!)
            state.month != null && state.dayOfMonth != null ->
                MonthDay.of(state.month!!, state.dayOfMonth!!)
            resolvedYear != null ->
                Year.of(resolvedYear)
            state.month != null ->
                Month.of(state.month!!)
            state.dayOfWeek != null ->
                DayOfWeek.of(state.dayOfWeek!!)
            effectiveOffset != null ->
                effectiveOffset
            else -> throw DateTimeParseException("Text '$text' could not be parsed: insufficient fields", text, 0)
        }
    }

    actual companion object {
        actual val ISO_LOCAL_DATE: DateTimeFormatter = DateTimeFormatter(KIND_ISO_DATE)
        actual val ISO_LOCAL_TIME: DateTimeFormatter = DateTimeFormatter(KIND_ISO_TIME)
        actual val ISO_LOCAL_DATE_TIME: DateTimeFormatter = DateTimeFormatter(KIND_ISO_DATE_TIME)
        actual val ISO_OFFSET_DATE_TIME: DateTimeFormatter = DateTimeFormatter(KIND_ISO_OFFSET_DATE_TIME)
        actual val ISO_ZONED_DATE_TIME: DateTimeFormatter = DateTimeFormatter(KIND_ISO_ZONED_DATE_TIME)
        actual val ISO_DATE_TIME: DateTimeFormatter = DateTimeFormatter(KIND_ISO_ZONED_DATE_TIME)
        actual val ISO_INSTANT: DateTimeFormatter = DateTimeFormatter(KIND_ISO_INSTANT)
        actual val BASIC_ISO_DATE: DateTimeFormatter = DateTimeFormatter(KIND_BASIC_ISO_DATE)

        actual fun ofPattern(pattern: String): DateTimeFormatter {
            // [Nullability Decision] pattern is non-null, matching JDK 8 DateTimeFormatter.ofPattern
            val (tokens, nextIdx) = compilePattern(pattern, 0, false)
            if (nextIdx != pattern.length) {
                throw IllegalArgumentException("Pattern contains unmatched ']': $pattern")
            }
            return DateTimeFormatter(KIND_PATTERN, tokens, Locale.getDefault(), null)
        }

        actual fun ofPattern(pattern: String, locale: Locale): DateTimeFormatter {
            val (tokens, nextIdx) = compilePattern(pattern, 0, false)
            if (nextIdx != pattern.length) {
                throw IllegalArgumentException("Pattern contains unmatched ']': $pattern")
            }
            return DateTimeFormatter(KIND_PATTERN, tokens, locale, null)
        }

        private fun compilePattern(pattern: String, startIndex: Int, inOptional: Boolean): Pair<List<PatternToken>, Int> {
            val tokens = mutableListOf<PatternToken>()
            var i = startIndex
            while (i < pattern.length) {
                val c = pattern[i]
                when {
                    c == '\'' -> {
                        if (i + 1 < pattern.length && pattern[i + 1] == '\'') {
                            tokens.add(LiteralToken("'"))
                            i += 2
                        } else {
                            val sb = StringBuilder()
                            i++
                            var closed = false
                            while (i < pattern.length) {
                                val q = pattern[i]
                                if (q == '\'') {
                                    if (i + 1 < pattern.length && pattern[i + 1] == '\'') {
                                        sb.append('\'')
                                        i += 2
                                    } else {
                                        i++
                                        closed = true
                                        break
                                    }
                                } else {
                                    sb.append(q)
                                    i++
                                }
                            }
                            if (!closed) {
                                throw IllegalArgumentException("Pattern ends with an unclosed string literal: $pattern")
                            }
                            tokens.add(LiteralToken(sb.toString()))
                        }
                    }
                    c == '[' -> {
                        val (optTokens, nextIdx) = compilePattern(pattern, i + 1, true)
                        if (optTokens.isNotEmpty()) {
                            tokens.add(OptionalToken(optTokens))
                        }
                        i = nextIdx
                    }
                    c == ']' -> {
                        if (!inOptional) {
                            throw IllegalArgumentException("Pattern contains unmatched ']': $pattern")
                        }
                        return tokens to (i + 1)
                    }
                    c == '{' || c == '}' || c == '#' -> {
                        throw IllegalArgumentException("Pattern includes reserved character: '$c'")
                    }
                    c in 'A'..'Z' || c in 'a'..'z' -> {
                        var j = i + 1
                        while (j < pattern.length && pattern[j] == c) j++
                        val count = j - i
                        validateFieldToken(c, count)
                        tokens.add(FieldToken(c, count))
                        i = j
                    }
                    else -> {
                        val sb = StringBuilder()
                        while (i < pattern.length) {
                            val ch = pattern[i]
                            if (ch == '\'' || ch == '[' || ch == ']' || ch == '{' || ch == '}' || ch == '#' || ch in 'A'..'Z' || ch in 'a'..'z') break
                            sb.append(ch)
                            i++
                        }
                        tokens.add(LiteralToken(sb.toString()))
                    }
                }
            }
            if (inOptional) {
                throw IllegalArgumentException("Pattern ends with an unclosed optional section '[': $pattern")
            }
            return tokens to i
        }

        private fun validateFieldToken(letter: Char, count: Int) {
            when (letter) {
                'G' -> if (count > 5) throw IllegalArgumentException("Too many pattern letters: $letter")
                'y', 'u', 'Y' -> if (count > 19) throw IllegalArgumentException("Too many pattern letters: $letter")
                'Q', 'q', 'M', 'L', 'E' -> if (count > 5) throw IllegalArgumentException("Too many pattern letters: $letter")
                'e' -> if (count > 5) throw IllegalArgumentException("Too many pattern letters: $letter")
                'c' -> if (count == 2 || count > 5) throw IllegalArgumentException("Invalid pattern count for c: $count")
                'd', 'H', 'k', 'K', 'h', 'm', 's', 'w' -> if (count > 2) throw IllegalArgumentException("Too many pattern letters: $letter")
                'D' -> if (count > 3) throw IllegalArgumentException("Too many pattern letters: $letter")
                'a', 'W', 'F' -> if (count > 1) throw IllegalArgumentException("Too many pattern letters: $letter")
                'S' -> if (count > 9) throw IllegalArgumentException("Too many pattern letters: $letter")
                'n', 'N', 'A' -> if (count > 19) throw IllegalArgumentException("Too many pattern letters: $letter")
                'V' -> if (count != 2) throw IllegalArgumentException("Pattern letter count must be 2: $letter")
                'z' -> if (count > 4) throw IllegalArgumentException("Too many pattern letters: $letter")
                'O' -> if (count != 1 && count != 4) throw IllegalArgumentException("Invalid pattern count for O: $count")
                'X', 'x', 'Z' -> if (count > 5) throw IllegalArgumentException("Too many pattern letters: $letter")
                else -> throw IllegalArgumentException("Unknown pattern letter: $letter")
            }
        }
    }
}

private fun yearOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalDate -> temporal.year
    is LocalDateTime -> temporal.year
    is OffsetDateTime -> temporal.toLocalDate().year
    is ZonedDateTime -> temporal.toLocalDate().year
    is Year -> temporal.value
    is YearMonth -> temporal.year
    else -> ChronoField.YEAR.getFrom(temporal).toInt()
}

private fun monthOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalDate -> temporal.monthValue
    is LocalDateTime -> temporal.monthValue
    is OffsetDateTime -> temporal.toLocalDate().monthValue
    is ZonedDateTime -> temporal.toLocalDate().monthValue
    is YearMonth -> temporal.monthValue
    is MonthDay -> temporal.monthValue
    is Month -> temporal.value
    else -> ChronoField.MONTH_OF_YEAR.getFrom(temporal).toInt()
}

private fun dayOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalDate -> temporal.dayOfMonth
    is LocalDateTime -> temporal.dayOfMonth
    is OffsetDateTime -> temporal.toLocalDate().dayOfMonth
    is ZonedDateTime -> temporal.toLocalDate().dayOfMonth
    is MonthDay -> temporal.dayOfMonth
    else -> ChronoField.DAY_OF_MONTH.getFrom(temporal).toInt()
}

private fun dayOfYearOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalDate -> temporal.dayOfYear
    is LocalDateTime -> temporal.dayOfYear
    is OffsetDateTime -> temporal.toLocalDate().dayOfYear
    is ZonedDateTime -> temporal.toLocalDate().dayOfYear
    else -> ChronoField.DAY_OF_YEAR.getFrom(temporal).toInt()
}

private fun dayOfWeekOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalDate -> temporal.dayOfWeek.value
    is LocalDateTime -> temporal.dayOfWeek.value
    is OffsetDateTime -> temporal.toLocalDate().dayOfWeek.value
    is ZonedDateTime -> temporal.toLocalDate().dayOfWeek.value
    is DayOfWeek -> temporal.value
    else -> ChronoField.DAY_OF_WEEK.getFrom(temporal).toInt()
}

private fun hourOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalTime -> temporal.hour
    is LocalDateTime -> temporal.hour
    is OffsetDateTime -> temporal.toLocalTime().hour
    is ZonedDateTime -> temporal.toLocalTime().hour
    is OffsetTime -> temporal.toLocalTime().hour
    else -> ChronoField.HOUR_OF_DAY.getFrom(temporal).toInt()
}

private fun minuteOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalTime -> temporal.minute
    is LocalDateTime -> temporal.minute
    is OffsetDateTime -> temporal.toLocalTime().minute
    is ZonedDateTime -> temporal.toLocalTime().minute
    is OffsetTime -> temporal.toLocalTime().minute
    else -> ChronoField.MINUTE_OF_HOUR.getFrom(temporal).toInt()
}

private fun secondOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalTime -> temporal.second
    is LocalDateTime -> temporal.second
    is OffsetDateTime -> temporal.toLocalTime().second
    is ZonedDateTime -> temporal.toLocalTime().second
    is OffsetTime -> temporal.toLocalTime().second
    else -> ChronoField.SECOND_OF_MINUTE.getFrom(temporal).toInt()
}

private fun nanoOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalTime -> temporal.nano
    is LocalDateTime -> temporal.nano
    is OffsetDateTime -> temporal.toLocalTime().nano
    is ZonedDateTime -> temporal.toLocalTime().nano
    is OffsetTime -> temporal.toLocalTime().nano
    is Instant -> temporal.nano
    else -> 0
}

private fun offsetOf(temporal: TemporalAccessor): ZoneOffset = when (temporal) {
    is OffsetDateTime -> temporal.offset
    is ZonedDateTime -> temporal.offset
    is OffsetTime -> temporal.offset
    is ZoneOffset -> temporal
    else -> throw DateTimeException("Unable to obtain ZoneOffset from TemporalAccessor: $temporal")
}

private fun offsetOfOrNull(temporal: TemporalAccessor): ZoneOffset? = when (temporal) {
    is OffsetDateTime -> temporal.offset
    is ZonedDateTime -> temporal.offset
    is OffsetTime -> temporal.offset
    is ZoneOffset -> temporal
    else -> null
}

private fun zoneIdOf(temporal: TemporalAccessor): ZoneId = when (temporal) {
    is ZonedDateTime -> temporal.zone
    is OffsetDateTime -> temporal.offset
    is ZoneOffset -> temporal
    else -> throw DateTimeException("Unable to obtain ZoneId from TemporalAccessor: $temporal")
}

private fun zoneIdOfOrNull(temporal: TemporalAccessor): ZoneId? = when (temporal) {
    is ZonedDateTime -> temporal.zone
    is OffsetDateTime -> temporal.offset
    is ZoneOffset -> temporal
    else -> null
}

private fun instantOf(temporal: TemporalAccessor): Instant = when (temporal) {
    is Instant -> temporal
    is OffsetDateTime -> temporal.toInstant()
    is ZonedDateTime -> temporal.toInstant()
    else -> throw DateTimeException("Unable to obtain Instant from TemporalAccessor: $temporal")
}

private fun formatInstant(instant: Instant): String {
    val ldt = LocalDateTime.ofEpochSecond(instant.epochSecond, instant.nano, ZoneOffset.UTC)
    return formatIsoDate(ldt.year, ldt.monthValue, ldt.dayOfMonth) + "T" +
        formatIsoTime(ldt.hour, ldt.minute, ldt.second, 0, true) +
        formatIsoInstantFraction(instant.nano) + "Z"
}

private fun parseIsoInstant(text: String): Instant {
    return parseOffsetDateTime(text).toInstant()
}
