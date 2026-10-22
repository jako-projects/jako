package java.time.format

import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.formatIsoDate
import java.time.formatIsoInstantFraction
import java.time.formatIsoTime
import java.time.parseIsoLocalDateString
import java.time.parseIsoLocalDateTimeString
import java.time.parseIsoLocalTimeString
import java.time.parseOffsetDateTime
import java.time.temporal.TemporalAccessor

private const val KIND_ISO_DATE = 1

private const val KIND_ISO_TIME = 2

private const val KIND_ISO_DATE_TIME = 3

private const val KIND_ISO_INSTANT = 4

private const val KIND_PATTERN = 5

private sealed class PatternToken

private data class FieldToken(val letter: Char, val count: Int) : PatternToken()

private data class LiteralToken(val text: String) : PatternToken()

actual class DateTimeFormatter private constructor(
    private val kind: Int,
    private val tokens: List<PatternToken> = emptyList()
) {
    actual fun format(temporal: TemporalAccessor): String {
        return when (kind) {
            KIND_ISO_DATE -> formatIsoDate(yearOf(temporal), monthOf(temporal), dayOf(temporal))
            KIND_ISO_TIME -> formatIsoTime(hourOf(temporal), minuteOf(temporal), secondOf(temporal), nanoOf(temporal), true)
            KIND_ISO_DATE_TIME -> formatIsoDate(yearOf(temporal), monthOf(temporal), dayOf(temporal)) +
                "T" + formatIsoTime(hourOf(temporal), minuteOf(temporal), secondOf(temporal), nanoOf(temporal), true)
            KIND_ISO_INSTANT -> formatInstant(instantOf(temporal))
            KIND_PATTERN -> formatPattern(temporal)
            else -> throw DateTimeException("Unknown formatter")
        }
    }

    actual fun parse(text: CharSequence): TemporalAccessor {
        val s = text.toString()
        return try {
            when (kind) {
                KIND_ISO_DATE -> parseIsoLocalDateString(s)
                KIND_ISO_TIME -> parseIsoLocalTimeString(s)
                KIND_ISO_DATE_TIME -> parseIsoLocalDateTimeString(s)
                KIND_ISO_INSTANT -> parseIsoInstant(s)
                KIND_PATTERN -> parsePattern(s)
                else -> throw DateTimeException("Unknown formatter")
            }
        } catch (ex: DateTimeParseException) {
            throw ex
        } catch (ex: DateTimeException) {
            throw DateTimeParseException("Text '$s' could not be parsed: ${ex.message}", s, 0)
        } catch (ex: RuntimeException) {
            throw DateTimeParseException("Text '$s' could not be parsed", s, 0)
        }
    }

    private fun formatPattern(temporal: TemporalAccessor): String {
        val sb = StringBuilder()
        for (token in tokens) {
            when (token) {
                is LiteralToken -> sb.append(token.text)
                is FieldToken -> sb.append(formatField(temporal, token.letter, token.count))
            }
        }
        return sb.toString()
    }

    private fun formatField(temporal: TemporalAccessor, letter: Char, count: Int): String {
        val value = when (letter) {
            'y' -> yearOf(temporal)
            'M' -> monthOf(temporal)
            'd' -> dayOf(temporal)
            'H' -> hourOf(temporal)
            'm' -> minuteOf(temporal)
            's' -> secondOf(temporal)
            else -> throw DateTimeException("Unknown pattern letter: $letter")
        }
        if (letter == 'y' && count == 2) {
            return (kotlin.math.abs(value) % 100).toString().padStart(2, '0')
        }
        val raw = if (letter == 'y' && value < 0) {
            value.toString()
        } else {
            kotlin.math.abs(value).toString()
        }
        val padded = if (value < 0 && letter == 'y') {
            val digits = raw.removePrefix("-")
            "-" + digits.padStart(count, '0')
        } else {
            raw.padStart(count, '0')
        }
        return padded
    }

    private fun parsePattern(text: String): TemporalAccessor {
        var pos = 0
        var year: Int? = null
        var month: Int? = null
        var day: Int? = null
        var hour: Int? = null
        var minute: Int? = null
        var second: Int? = null
        for (token in tokens) {
            when (token) {
                is LiteralToken -> {
                    val lit = token.text
                    if (pos + lit.length > text.length || text.substring(pos, pos + lit.length) != lit) {
                        throw DateTimeParseException("Text '$text' could not be parsed", text, pos)
                    }
                    pos += lit.length
                }
                is FieldToken -> {
                    val parsed = parseField(text, pos, token.letter, token.count)
                    pos = parsed.second
                    when (token.letter) {
                        'y' -> year = parsed.first
                        'M' -> month = parsed.first
                        'd' -> day = parsed.first
                        'H' -> hour = parsed.first
                        'm' -> minute = parsed.first
                        's' -> second = parsed.first
                    }
                }
            }
        }
        if (pos != text.length) {
            throw DateTimeParseException("Text '$text' could not be parsed, unparsed text found", text, pos)
        }
        val hasDate = year != null && month != null && day != null
        val hasTime = hour != null || minute != null || second != null
        return when {
            hasDate && hasTime -> LocalDateTime.of(
                year!!, month!!, day!!,
                hour ?: 0, minute ?: 0, second ?: 0
            )
            hasDate -> LocalDate.of(year!!, month!!, day!!)
            hour != null && minute != null -> LocalTime.of(hour, minute, second ?: 0)
            else -> throw DateTimeParseException("Text '$text' could not be parsed: insufficient fields", text, 0)
        }
    }

    private fun parseField(text: String, start: Int, letter: Char, count: Int): Pair<Int, Int> {
        if (start >= text.length) {
            throw DateTimeParseException("Text '$text' could not be parsed", text, start)
        }
        var i = start
        val maxDigits = when {
            letter == 'y' && count >= 4 -> 10
            letter == 'y' && count == 2 -> 2
            count >= 2 -> 2
            else -> 2
        }
        val minDigits = when {
            letter == 'y' && count >= 4 -> 4
            count >= 2 -> count.coerceAtMost(2)
            else -> 1
        }
        if (letter == 'y' && i < text.length && (text[i] == '+' || text[i] == '-')) {
            i++
        }
        val digitStart = i
        while (i < text.length && i - digitStart < maxDigits && text[i] in '0'..'9') {
            i++
        }
        if (i - digitStart < minDigits) {
            throw DateTimeParseException("Text '$text' could not be parsed", text, start)
        }
        val number = text.substring(start, i).toIntOrNull()
            ?: throw DateTimeParseException("Text '$text' could not be parsed", text, start)
        val value = if (letter == 'y' && count == 2 && number in 0..99) {
            2000 + number
        } else {
            number
        }
        return value to i
    }

    actual companion object {
        actual val ISO_LOCAL_DATE: DateTimeFormatter = DateTimeFormatter(KIND_ISO_DATE)
        actual val ISO_LOCAL_TIME: DateTimeFormatter = DateTimeFormatter(KIND_ISO_TIME)
        actual val ISO_LOCAL_DATE_TIME: DateTimeFormatter = DateTimeFormatter(KIND_ISO_DATE_TIME)
        actual val ISO_INSTANT: DateTimeFormatter = DateTimeFormatter(KIND_ISO_INSTANT)

        actual fun ofPattern(pattern: String): DateTimeFormatter {
            // [Nullability Decision] pattern is non-null, matching JDK 8 DateTimeFormatter.ofPattern
            return DateTimeFormatter(KIND_PATTERN, compilePattern(pattern))
        }

        private fun compilePattern(pattern: String): List<PatternToken> {
            val tokens = mutableListOf<PatternToken>()
            var i = 0
            while (i < pattern.length) {
                val c = pattern[i]
                when {
                    c == '\'' -> {
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
                    c in 'A'..'Z' || c in 'a'..'z' -> {
                        var j = i + 1
                        while (j < pattern.length && pattern[j] == c) j++
                        if (c !in "yMdHms") {
                            throw IllegalArgumentException("Unknown pattern letter: $c")
                        }
                        tokens.add(FieldToken(c, j - i))
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
                        tokens.add(LiteralToken(sb.toString()))
                    }
                }
            }
            return tokens
        }
    }
}

private fun yearOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalDate -> temporal.year
    is LocalDateTime -> temporal.year
    is OffsetDateTime -> temporal.toLocalDate().year
    is ZonedDateTime -> temporal.toLocalDate().year
    else -> throw DateTimeException("Unsupported field: YearOfEra")
}

private fun monthOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalDate -> temporal.monthValue
    is LocalDateTime -> temporal.monthValue
    is OffsetDateTime -> temporal.toLocalDate().monthValue
    is ZonedDateTime -> temporal.toLocalDate().monthValue
    else -> throw DateTimeException("Unsupported field: MonthOfYear")
}

private fun dayOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalDate -> temporal.dayOfMonth
    is LocalDateTime -> temporal.dayOfMonth
    is OffsetDateTime -> temporal.toLocalDate().dayOfMonth
    is ZonedDateTime -> temporal.toLocalDate().dayOfMonth
    else -> throw DateTimeException("Unsupported field: DayOfMonth")
}

private fun hourOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalTime -> temporal.hour
    is LocalDateTime -> temporal.hour
    is OffsetDateTime -> temporal.toLocalTime().hour
    is ZonedDateTime -> temporal.toLocalTime().hour
    else -> throw DateTimeException("Unsupported field: HourOfDay")
}

private fun minuteOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalTime -> temporal.minute
    is LocalDateTime -> temporal.minute
    is OffsetDateTime -> temporal.toLocalTime().minute
    is ZonedDateTime -> temporal.toLocalTime().minute
    else -> throw DateTimeException("Unsupported field: MinuteOfHour")
}

private fun secondOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalTime -> temporal.second
    is LocalDateTime -> temporal.second
    is OffsetDateTime -> temporal.toLocalTime().second
    is ZonedDateTime -> temporal.toLocalTime().second
    else -> throw DateTimeException("Unsupported field: SecondOfMinute")
}

private fun nanoOf(temporal: TemporalAccessor): Int = when (temporal) {
    is LocalTime -> temporal.nano
    is LocalDateTime -> temporal.nano
    is OffsetDateTime -> temporal.toLocalTime().nano
    is ZonedDateTime -> temporal.toLocalTime().nano
    is Instant -> temporal.nano
    else -> 0
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
