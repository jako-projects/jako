package java.util.regex

import java.io.Serializable

actual class Pattern constructor(
    val patternStr: String,
    val regex: Regex,
    val flags: Int = 0
) : Serializable {
    internal val capturingGroupCount: Int
    internal val namedGroups: Map<String, Int>

    init {
        val (count, names) = analyzeGroups(patternStr, (flags and LITERAL) != 0)
        capturingGroupCount = count
        namedGroups = names
    }

    actual fun pattern(): String = patternStr
    actual fun flags(): Int = flags
    actual fun matcher(input: CharSequence): Matcher = Matcher(this, input)

    actual fun split(input: CharSequence): Array<String> = split(input, 0)

    actual fun split(input: CharSequence, limit: Int): Array<String> {
        val list = if (limit > 0) {
            regex.split(input, limit)
        } else {
            regex.split(input, 0)
        }
        if (limit == 0 && list.size > 1) {
            var end = list.size
            while (end > 0 && list[end - 1].isEmpty()) {
                end--
            }
            return list.subList(0, end).toTypedArray()
        }
        return list.toTypedArray()
    }

    override fun toString(): String = patternStr

    actual companion object {
        actual const val UNIX_LINES = 1
        actual const val CASE_INSENSITIVE = 2
        actual const val COMMENTS = 4
        actual const val MULTILINE = 8
        actual const val LITERAL = 16
        actual const val DOTALL = 32
        actual const val UNICODE_CASE = 64
        actual const val CANON_EQ = 128
        actual const val UNICODE_CHARACTER_CLASS = 256

        actual fun compile(pattern: String): Pattern = compile(pattern, 0)

        actual fun compile(pattern: String, flags: Int): Pattern {
            val options = mutableSetOf<RegexOption>()
            if (flags and CASE_INSENSITIVE != 0) options.add(RegexOption.IGNORE_CASE)
            if (flags and MULTILINE != 0) options.add(RegexOption.MULTILINE)

            val effectivePattern = preprocessPattern(pattern, flags)
            val compiled = try {
                Regex(effectivePattern, options)
            } catch (e: Throwable) {
                if (e is PatternSyntaxException) throw e
                throw PatternSyntaxException(e.message ?: "Invalid regex pattern", pattern, -1)
            }
            return Pattern(pattern, compiled, flags)
        }

        private const val SYNTAX_CHARS_OUTSIDE_CLASS = "^$\\.*+?()[]{}|/"
        private const val SYNTAX_CHARS_INSIDE_CLASS = "^$\\.*+?()[]{}|/-"

        private fun appendEscapedLiteralChar(sb: kotlin.text.StringBuilder, ch: Char, inCharClass: Boolean) {
            val allowed = if (inCharClass) SYNTAX_CHARS_INSIDE_CLASS else SYNTAX_CHARS_OUTSIDE_CLASS
            if (allowed.indexOf(ch) >= 0) {
                sb.append('\\').append(ch)
            } else {
                sb.append(ch)
            }
        }

        private fun preprocessPattern(pattern: String, flags: Int): String {
            if (flags and LITERAL != 0) {
                val sb = kotlin.text.StringBuilder(pattern.length * 2)
                for (ch in pattern) {
                    appendEscapedLiteralChar(sb, ch, inCharClass = false)
                }
                return sb.toString()
            }
            val dotAll = (flags and DOTALL) != 0
            val comments = (flags and COMMENTS) != 0
            val sb = kotlin.text.StringBuilder(pattern.length)
            var inCharClass = false
            var i = 0
            while (i < pattern.length) {
                val ch = pattern[i]
                if (ch == '\\') {
                    if (i + 1 < pattern.length) {
                        val next = pattern[i + 1]
                        if (next == 'Q') {
                            val endQuote = pattern.indexOf("\\E", i + 2)
                            val literalEnd = if (endQuote >= 0) endQuote else pattern.length
                            for (k in (i + 2) until literalEnd) {
                                appendEscapedLiteralChar(sb, pattern[k], inCharClass)
                            }
                            i = if (endQuote >= 0) endQuote + 2 else pattern.length
                            continue
                        }
                        if (next == 'E') {
                            i += 2
                            continue
                        }
                        if (next == 'a') {
                            sb.append("\\x07")
                            i += 2
                            continue
                        }
                        if (next == 'e') {
                            sb.append("\\x1B")
                            i += 2
                            continue
                        }
                        if (!inCharClass) {
                            when (next) {
                                'A' -> {
                                    sb.append('^')
                                    i += 2
                                    continue
                                }
                                'z' -> {
                                    sb.append('$')
                                    i += 2
                                    continue
                                }
                                'Z' -> {
                                    sb.append("(?=\\r?\\n?$)")
                                    i += 2
                                    continue
                                }
                            }
                        }
                        if (next.code in 0x21..0x7E && !next.isLetterOrDigit()) {
                            appendEscapedLiteralChar(sb, next, inCharClass)
                        } else {
                            sb.append('\\').append(next)
                        }
                    } else {
                        sb.append('\\')
                    }
                    i += 2
                    continue
                }
                if (ch == '[') {
                    inCharClass = true
                    sb.append(ch)
                    i++
                    continue
                }
                if (ch == ']' && inCharClass) {
                    inCharClass = false
                    sb.append(ch)
                    i++
                    continue
                }
                if (!inCharClass) {
                    if (comments) {
                        if (ch.isWhitespace()) {
                            i++
                            continue
                        }
                        if (ch == '#') {
                            while (i < pattern.length && pattern[i] != '\n') {
                                i++
                            }
                            continue
                        }
                    }
                    if (dotAll && ch == '.') {
                        sb.append("[\\s\\S]")
                        i++
                        continue
                    }
                }
                sb.append(ch)
                i++
            }
            return sb.toString()
        }

        actual fun matches(regex: String, input: CharSequence): Boolean {
            return compile(regex).matcher(input).matches()
        }

        actual fun quote(s: String): String {
            val slashEIndex = s.indexOf("\\E")
            if (slashEIndex == -1) return "\\Q$s\\E"
            val sb = kotlin.text.StringBuilder(s.length * 2)
            sb.append("\\Q")
            var current = 0
            var next = slashEIndex
            while (next != -1) {
                sb.append(s, current, next)
                current = next + 2
                sb.append("\\E\\\\E\\Q")
                next = s.indexOf("\\E", current)
            }
            sb.append(s, current, s.length)
            sb.append("\\E")
            return sb.toString()
        }

        private fun analyzeGroups(pattern: String, isLiteral: Boolean): Pair<Int, Map<String, Int>> {
            if (isLiteral) return 0 to emptyMap()
            var count = 0
            val names = mutableMapOf<String, Int>()
            var inCharClass = false
            var i = 0
            while (i < pattern.length) {
                val ch = pattern[i]
                if (ch == '\\') {
                    if (i + 1 < pattern.length && pattern[i + 1] == 'Q') {
                        val endQuote = pattern.indexOf("\\E", i + 2)
                        i = if (endQuote >= 0) endQuote + 2 else pattern.length
                    } else {
                        i += 2
                    }
                    continue
                }
                if (ch == '[') {
                    inCharClass = true
                } else if (ch == ']') {
                    inCharClass = false
                } else if (ch == '(' && !inCharClass) {
                    if (i + 1 < pattern.length && pattern[i + 1] == '?') {
                        if (i + 2 < pattern.length && pattern[i + 2] == '<' &&
                            (i + 3 >= pattern.length || (pattern[i + 3] != '=' && pattern[i + 3] != '!'))
                        ) {
                            val closeIdx = pattern.indexOf('>', i + 3)
                            if (closeIdx > i + 3) {
                                count++
                                val name = pattern.substring(i + 3, closeIdx)
                                names[name] = count
                            }
                        }
                    } else {
                        count++
                    }
                }
                i++
            }
            return count to names
        }
    }
}
