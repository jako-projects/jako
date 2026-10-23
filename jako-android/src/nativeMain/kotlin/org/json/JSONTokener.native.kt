package org.json

import kotlin.math.floor

actual open class JSONTokener actual constructor(
    private val inString: String?
) {
    private var pos = 0

    actual open fun nextValue(): Any {
        val c = nextCleanInternal()
        return when (c) {
            (-1).toChar() -> throw syntaxError("End of input")
            '{'.code.toChar() -> readObject()
            '['.code.toChar() -> readArray()
            '\'', '"' -> nextString(c)
            else -> {
                pos--
                readLiteral()
            }
        }
    }

    private fun nextCleanInternal(): Char {
        while (inString != null && pos < inString.length) {
            val c = inString[pos++]
            when (c) {
                '\t', ' ', '\n', '\r' -> continue
                '/' -> {
                    if (pos == inString.length) return c
                    when (inString[pos]) {
                        '*' -> {
                            pos++
                            val commentEnd = inString.indexOf("*/", pos)
                            if (commentEnd == -1) {
                                throw syntaxError("Unterminated comment")
                            }
                            pos = commentEnd + 2
                            continue
                        }
                        '/' -> {
                            pos++
                            skipToEndOfLine()
                            continue
                        }
                        else -> return c
                    }
                }
                '#' -> {
                    skipToEndOfLine()
                    continue
                }
                else -> return c
            }
        }
        return (-1).toChar()
    }

    private fun skipToEndOfLine() {
        if (inString == null) return
        while (pos < inString.length) {
            val c = inString[pos++]
            if (c == '\r' || c == '\n') break
        }
    }

    actual open fun nextString(quote: Char): String {
        val str = inString ?: throw syntaxError("Unterminated string")
        var builder: StringBuilder? = null
        var start = pos
        while (pos < str.length) {
            val c = str[pos++]
            if (c == quote) {
                return if (builder == null) {
                    str.substring(start, pos - 1)
                } else {
                    builder.append(str, start, pos - 1)
                    builder.toString()
                }
            }
            if (c == '\\') {
                if (pos == str.length) throw syntaxError("Unterminated escape sequence")
                if (builder == null) builder = StringBuilder()
                builder.append(str, start, pos - 1)
                builder.append(readEscapeCharacter())
                start = pos
            }
        }
        throw syntaxError("Unterminated string")
    }

    private fun readEscapeCharacter(): Char {
        val str = inString!!
        return when (val escaped = str[pos++]) {
            'u' -> {
                if (pos + 4 > str.length) throw syntaxError("Unterminated escape sequence")
                val hex = str.substring(pos, pos + 4)
                pos += 4
                try {
                    hex.toInt(16).toChar()
                } catch (_: NumberFormatException) {
                    throw syntaxError("Invalid escape sequence: \\u$hex")
                }
            }
            't' -> '\t'
            'b' -> '\b'
            'n' -> '\n'
            'r' -> '\r'
            'f' -> '\u000c'
            '\'', '"', '\\', '/' -> escaped
            else -> escaped
        }
    }

    private fun readLiteral(): Any {
        val literal = nextToInternal("{}[]/\\:,=;# \t\u000c")
        if (literal.isEmpty()) throw syntaxError("Expected literal value")
        if (literal.equals("null", ignoreCase = true)) return JSONObject.NULL
        if (literal.equals("true", ignoreCase = true)) return true
        if (literal.equals("false", ignoreCase = true)) return false

        if ('.' !in literal && 'e' !in literal && 'E' !in literal) {
            var base = 10
            var number = literal
            if (number.startsWith("0x") || number.startsWith("0X")) {
                number = number.substring(2)
                base = 16
            } else if (number.startsWith("0") && number.length > 1) {
                number = number.substring(1)
                base = 8
            }
            val longValue = number.toLongOrNull(base)
            if (longValue != null) {
                return if (longValue <= Int.MAX_VALUE && longValue >= Int.MIN_VALUE) {
                    longValue.toInt()
                } else {
                    longValue
                }
            }
        }
        val doubleVal = literal.toDoubleOrNull()
        if (doubleVal != null && !doubleVal.isInfinite() && !doubleVal.isNaN()) {
            return doubleVal
        }
        return literal
    }

    private fun nextToInternal(excluded: String): String {
        val str = inString ?: return ""
        val start = pos
        while (pos < str.length) {
            val c = str[pos]
            if (c == '\r' || c == '\n' || excluded.indexOf(c) != -1) {
                return str.substring(start, pos)
            }
            pos++
        }
        return str.substring(start)
    }

    private fun readObject(): JSONObject {
        val result = JSONObject()
        val first = nextCleanInternal()
        if (first == '}') return result
        if (first != (-1).toChar()) pos--

        while (true) {
            val name = nextValue()
            if (name !is String) {
                throw syntaxError("Names must be strings, but $name is of type ${name::class.simpleName}")
            }
            val separator = nextCleanInternal()
            if (separator != ':' && separator != '=') {
                throw syntaxError("Expected ':' after $name")
            }
            if (inString != null && pos < inString.length && inString[pos] == '>') {
                pos++
            }
            result.put(name, nextValue())

            when (nextCleanInternal()) {
                '}' -> return result
                ';', ',' -> {
                    if (nextCleanInternal() == '}') return result
                    pos--
                }
                else -> throw syntaxError("Unterminated object")
            }
        }
    }

    private fun readArray(): JSONArray {
        val result = JSONArray()
        var hasTrailingSeparator = false
        while (true) {
            when (val c = nextCleanInternal()) {
                (-1).toChar() -> throw syntaxError("Unterminated array")
                ']' -> {
                    if (hasTrailingSeparator) result.put(null as Any?)
                    return result
                }
                ',', ';' -> {
                    result.put(null as Any?)
                    hasTrailingSeparator = true
                }
                else -> {
                    pos--
                    result.put(nextValue())
                    when (nextCleanInternal()) {
                        ']' -> return result
                        ',', ';' -> hasTrailingSeparator = true
                        else -> throw syntaxError("Unterminated array")
                    }
                }
            }
        }
    }

    actual open fun syntaxError(message: String): JSONException {
        return JSONException(message + this)
    }

    override fun toString(): String {
        return " at character $pos of $inString"
    }

    actual open fun more(): Boolean = inString != null && pos < inString.length

    actual open fun next(): Char {
        return if (inString != null && pos < inString.length) inString[pos++] else '\u0000'
    }

    actual open fun next(c: Char): Char {
        val result = next()
        if (result != c) throw syntaxError("Expected $c but was $result")
        return result
    }

    actual open fun nextClean(): Char {
        val nextCleanInt = nextCleanInternal()
        return if (nextCleanInt == (-1).toChar()) '\u0000' else nextCleanInt
    }

    actual open fun next(length: Int): String {
        if (inString == null || pos + length > inString.length) {
            throw syntaxError("$length is out of bounds")
        }
        val result = inString.substring(pos, pos + length)
        pos += length
        return result
    }

    actual open fun nextTo(excluded: String): String = nextToInternal(excluded).trim()

    actual open fun nextTo(excluded: Char): String = nextToInternal(excluded.toString()).trim()

    actual open fun skipPast(thru: String) {
        val str = inString ?: return
        val thruStart = str.indexOf(thru, pos)
        pos = if (thruStart == -1) str.length else thruStart + thru.length
    }

    actual open fun skipTo(to: Char): Char {
        val str = inString ?: return '\u0000'
        val index = str.indexOf(to, pos)
        return if (index != -1) {
            pos = index
            to
        } else {
            '\u0000'
        }
    }

    actual open fun back() {
        if (--pos == -1) pos = 0
    }

    actual companion object {
        actual fun dehexchar(hex: Char): Int = when (hex) {
            in '0'..'9' -> hex - '0'
            in 'A'..'F' -> hex - 'A' + 10
            in 'a'..'f' -> hex - 'a' + 10
            else -> -1
        }
    }
}
