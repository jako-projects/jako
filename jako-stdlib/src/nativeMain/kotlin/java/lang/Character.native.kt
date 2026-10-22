package java.lang

actual object Character {
    actual fun isISOControl(codePoint: Int): Boolean = codePoint in 0..0x1F || codePoint in 0x7F..0x9F
    actual fun isISOControl(ch: Char): Boolean = ch.isISOControl()
    actual fun isDigit(ch: Char): Boolean = ch.isDigit()
    actual fun isDigit(codePoint: Int): Boolean = codePoint in 0..0xFFFF && codePoint.toChar().isDigit()
    actual fun isLetter(ch: Char): Boolean = ch.isLetter()
    actual fun isLetter(codePoint: Int): Boolean = codePoint in 0..0xFFFF && codePoint.toChar().isLetter()
    actual fun isLetterOrDigit(ch: Char): Boolean = ch.isLetterOrDigit()
    actual fun isLetterOrDigit(codePoint: Int): Boolean = codePoint in 0..0xFFFF && codePoint.toChar().isLetterOrDigit()
    actual fun isWhitespace(ch: Char): Boolean = ch.isWhitespace() || ch == '\u0009' || ch == '\n' || ch == '\u000B' || ch == '\u000C' || ch == '\r' || ch == '\u001C' || ch == '\u001D' || ch == '\u001E' || ch == '\u001F'
    actual fun isWhitespace(codePoint: Int): Boolean = codePoint in 0..0xFFFF && isWhitespace(codePoint.toChar())
    actual fun toLowerCase(ch: Char): Char = ch.lowercaseChar()
    actual fun toLowerCase(codePoint: Int): Int = if (codePoint in 0..0xFFFF) codePoint.toChar().lowercaseChar().code else codePoint
    actual fun toUpperCase(ch: Char): Char = ch.uppercaseChar()
    actual fun toUpperCase(codePoint: Int): Int = if (codePoint in 0..0xFFFF) codePoint.toChar().uppercaseChar().code else codePoint
    actual fun digit(ch: Char, radix: Int): Int = ch.digitToIntOrNull(radix) ?: -1
    actual fun digit(codePoint: Int, radix: Int): Int = if (codePoint in 0..0xFFFF) digit(codePoint.toChar(), radix) else -1
    actual fun forDigit(digit: Int, radix: Int): Char {
        if (digit >= radix || digit < 0 || radix < 2 || radix > 36) return '\u0000'
        return if (digit < 10) ('0'.code + digit).toChar() else ('a'.code - 10 + digit).toChar()
    }
    actual fun charCount(codePoint: Int): Int = if (codePoint >= 0x10000) 2 else 1
    actual fun toChars(codePoint: Int): CharArray {
        if (codePoint < 0 || codePoint > 0x10FFFF) throw IllegalArgumentException()
        if (codePoint < 0x10000) return charArrayOf(codePoint.toChar())
        val offset = codePoint - 0x10000
        return charArrayOf(((offset ushr 10) + 0xD800).toChar(), ((offset and 0x3FF) + 0xDC00).toChar())
    }
    actual fun codePointAt(seq: CharSequence, index: Int): Int {
        val c1 = seq[index]
        if (c1.isHighSurrogate() && index + 1 < seq.length) {
            val c2 = seq[index + 1]
            if (c2.isLowSurrogate()) {
                return ((c1.code - 0xD800) shl 10) + (c2.code - 0xDC00) + 0x10000
            }
        }
        return c1.code
    }
}
