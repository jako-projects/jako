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
            if (flags and DOTALL != 0) options.add(RegexOption.DOT_MATCHES_ALL)
            if (flags and COMMENTS != 0) options.add(RegexOption.COMMENTS)
            if (flags and LITERAL != 0) options.add(RegexOption.LITERAL)

            val compiled = try {
                Regex(pattern, options)
            } catch (e: IllegalArgumentException) {
                throw PatternSyntaxException(e.message ?: "Invalid regex pattern", pattern, -1)
            }
            return Pattern(pattern, compiled, flags)
        }

        actual fun matches(regex: String, input: CharSequence): Boolean {
            return compile(regex).matcher(input).matches()
        }

        actual fun quote(s: String): String = Regex.escape(s)

        private fun analyzeGroups(pattern: String, isLiteral: Boolean): Pair<Int, Map<String, Int>> {
            if (isLiteral) return 0 to emptyMap()
            var count = 0
            val names = mutableMapOf<String, Int>()
            var inCharClass = false
            var i = 0
            while (i < pattern.length) {
                val ch = pattern[i]
                if (ch == '\\') {
                    i += 2
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
