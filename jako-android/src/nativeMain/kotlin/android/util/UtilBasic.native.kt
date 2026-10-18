package android.util

import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern
import kotlin.math.abs

actual class Log private constructor() {
    actual companion object {
        actual val VERBOSE: Int = 2
        actual val DEBUG: Int = 3
        actual val INFO: Int = 4
        actual val WARN: Int = 5
        actual val ERROR: Int = 6
        actual val ASSERT: Int = 7

        actual fun v(tag: String?, msg: String): Int = println(VERBOSE, tag, msg)

        actual fun v(tag: String?, msg: String?, tr: Throwable?): Int =
            println(VERBOSE, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        actual fun d(tag: String?, msg: String): Int = println(DEBUG, tag, msg)

        actual fun d(tag: String?, msg: String?, tr: Throwable?): Int =
            println(DEBUG, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        actual fun i(tag: String?, msg: String): Int = println(INFO, tag, msg)

        actual fun i(tag: String?, msg: String?, tr: Throwable?): Int =
            println(INFO, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        actual fun w(tag: String?, msg: String): Int = println(WARN, tag, msg)

        actual fun w(tag: String?, msg: String?, tr: Throwable?): Int =
            println(WARN, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        actual fun w(tag: String?, tr: Throwable?): Int =
            println(WARN, tag, getStackTraceString(tr))

        actual fun e(tag: String?, msg: String): Int = println(ERROR, tag, msg)

        actual fun e(tag: String?, msg: String?, tr: Throwable?): Int =
            println(ERROR, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        actual fun wtf(tag: String?, msg: String?): Int = wtf(tag, msg, null)

        actual fun wtf(tag: String?, tr: Throwable): Int = wtf(tag, tr.message, tr)

        actual fun wtf(tag: String?, msg: String?, tr: Throwable?): Int =
            println(ASSERT, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        actual fun getStackTraceString(tr: Throwable?): String {
            if (tr == null) return ""
            var t: Throwable? = tr
            while (t != null) {
                if (t is UnknownHostException) {
                    return ""
                }
                t = t.cause
            }
            return tr.stackTraceToString()
        }

        actual fun println(priority: Int, tag: String?, msg: String): Int {
            val levelChar = when (priority) {
                VERBOSE -> 'V'
                DEBUG -> 'D'
                INFO -> 'I'
                WARN -> 'W'
                ERROR -> 'E'
                ASSERT -> 'A'
                else -> '?'
            }
            val formatted = "$levelChar/${tag ?: ""}: $msg"
            kotlin.io.println(formatted)
            return formatted.encodeToByteArray().size
        }

        actual fun isLoggable(tag: String?, level: Int): Boolean {
            return level >= INFO
        }
    }
}

actual open class Pair<F, S> actual constructor(
    actual val first: F,
    actual val second: S
) {
    actual override fun equals(other: Any?): Boolean {
        if (other !is Pair<*, *>) return false
        return other.first == first && other.second == second
    }

    actual override fun hashCode(): Int {
        return (first?.hashCode() ?: 0) xor (second?.hashCode() ?: 0)
    }

    actual override fun toString(): String {
        return "Pair{$first $second}"
    }

    actual companion object {
        actual fun <A, B> create(a: A, b: B): Pair<A, B> = Pair(a, b)
    }
}

actual final class Size actual constructor(
    actual val width: Int,
    actual val height: Int
) {
    actual fun getWidth(): Int = width
    actual fun getHeight(): Int = height

    actual override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other is Size) {
            return width == other.width && height == other.height
        }
        return false
    }

    actual override fun toString(): String = "${width}x${height}"

    actual override fun hashCode(): Int {
        return height xor ((width shl (Int.SIZE_BITS / 2)) or (width ushr (Int.SIZE_BITS / 2)))
    }

    actual companion object {
        private fun invalidSize(s: String): Nothing {
            throw NumberFormatException("Invalid Size: \"$s\"")
        }

        actual fun parseSize(string: String): Size {
            var sepIx = string.indexOf('*')
            if (sepIx < 0) {
                sepIx = string.indexOf('x')
            }
            if (sepIx < 0) {
                invalidSize(string)
            }
            try {
                return Size(
                    string.substring(0, sepIx).toInt(),
                    string.substring(sepIx + 1).toInt()
                )
            } catch (e: NumberFormatException) {
                invalidSize(string)
            }
        }
    }
}

actual final class SizeF actual constructor(
    actual val width: Float,
    actual val height: Float
) {
    init {
        require(!width.isNaN() && !width.isInfinite()) { "width must be finite" }
        require(!height.isNaN() && !height.isInfinite()) { "height must be finite" }
    }

    actual fun getWidth(): Float = width
    actual fun getHeight(): Float = height

    actual override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other is SizeF) {
            return width == other.width && height == other.height
        }
        return false
    }

    actual override fun toString(): String = "${width}x${height}"

    actual override fun hashCode(): Int {
        return width.toBits() xor height.toBits()
    }

    actual companion object {
        private fun invalidSizeF(s: String): Nothing {
            throw NumberFormatException("Invalid SizeF: \"$s\"")
        }

        actual fun parseSizeF(string: String): SizeF {
            var sepIx = string.indexOf('*')
            if (sepIx < 0) {
                sepIx = string.indexOf('x')
            }
            if (sepIx < 0) {
                invalidSizeF(string)
            }
            try {
                return SizeF(
                    string.substring(0, sepIx).toFloat(),
                    string.substring(sepIx + 1).toFloat()
                )
            } catch (e: NumberFormatException) {
                invalidSizeF(string)
            } catch (e: IllegalArgumentException) {
                invalidSizeF(string)
            }
        }
    }
}

actual final class Rational actual constructor(
    numerator: Int,
    denominator: Int
) : Number(), Comparable<Rational> {

    actual val numerator: Int
    actual val denominator: Int

    init {
        var num = numerator
        var den = denominator
        if (den < 0) {
            num = -num
            den = -den
        }
        if (den == 0 && num > 0) {
            this.numerator = 1
            this.denominator = 0
        } else if (den == 0 && num < 0) {
            this.numerator = -1
            this.denominator = 0
        } else if (den == 0 && num == 0) {
            this.numerator = 0
            this.denominator = 0
        } else if (num == 0) {
            this.numerator = 0
            this.denominator = 1
        } else {
            val gcd = gcd(num, den)
            this.numerator = num / gcd
            this.denominator = den / gcd
        }
    }

    actual fun getNumerator(): Int = numerator
    actual fun getDenominator(): Int = denominator

    actual val isNaN: Boolean
        get() = denominator == 0 && numerator == 0

    actual fun isNaN(): Boolean = isNaN

    actual val isInfinite: Boolean
        get() = numerator != 0 && denominator == 0

    actual fun isInfinite(): Boolean = isInfinite

    actual val isFinite: Boolean
        get() = denominator != 0

    actual fun isFinite(): Boolean = isFinite

    actual val isZero: Boolean
        get() = isFinite && numerator == 0

    actual fun isZero(): Boolean = isZero

    private val isPosInf: Boolean
        get() = denominator == 0 && numerator > 0

    private val isNegInf: Boolean
        get() = denominator == 0 && numerator < 0

    actual override fun equals(other: Any?): Boolean {
        return other is Rational && numerator == other.numerator && denominator == other.denominator
    }

    actual override fun toString(): String {
        return when {
            isNaN -> "NaN"
            isPosInf -> "Infinity"
            isNegInf -> "-Infinity"
            else -> "$numerator/$denominator"
        }
    }

    actual override fun hashCode(): Int {
        val numeratorFlipped = (numerator shl 16) or (numerator ushr 16)
        return denominator xor numeratorFlipped
    }

    actual override fun toDouble(): Double = numerator.toDouble() / denominator.toDouble()

    actual override fun toFloat(): Float = numerator.toFloat() / denominator.toFloat()

    actual override fun toInt(): Int = when {
        isPosInf -> Int.MAX_VALUE
        isNegInf -> Int.MIN_VALUE
        isNaN -> 0
        else -> numerator / denominator
    }

    actual override fun toLong(): Long = when {
        isPosInf -> Long.MAX_VALUE
        isNegInf -> Long.MIN_VALUE
        isNaN -> 0L
        else -> (numerator / denominator).toLong()
    }

    actual override fun toShort(): Short = toInt().toShort()

    actual override fun toByte(): Byte = toInt().toByte()

    @Suppress("DEPRECATION")
    actual override fun toChar(): Char = toInt().toChar()

    actual override fun compareTo(other: Rational): Int {
        if (equals(other)) return 0
        if (isNaN) return 1
        if (other.isNaN) return -1
        if (isPosInf || other.isNegInf) return 1
        if (isNegInf || other.isPosInf) return -1

        val thisNumerator = numerator.toLong() * other.denominator.toLong()
        val otherNumerator = other.numerator.toLong() * denominator.toLong()
        return thisNumerator.compareTo(otherNumerator)
    }

    actual companion object {
        actual val NaN: Rational = Rational(0, 0)
        actual val POSITIVE_INFINITY: Rational = Rational(1, 0)
        actual val NEGATIVE_INFINITY: Rational = Rational(-1, 0)
        actual val ZERO: Rational = Rational(0, 1)

        fun gcd(numerator: Int, denominator: Int): Int {
            var a = numerator
            var b = denominator
            while (b != 0) {
                val oldB = b
                b = a % b
                a = oldB
            }
            return abs(a)
        }

        private fun invalidRational(s: String): Nothing {
            throw NumberFormatException("Invalid Rational: \"$s\"")
        }

        actual fun parseRational(string: String): Rational {
            when (string) {
                "NaN" -> return NaN
                "Infinity" -> return POSITIVE_INFINITY
                "-Infinity" -> return NEGATIVE_INFINITY
            }
            var sepIx = string.indexOf(':')
            if (sepIx < 0) {
                sepIx = string.indexOf('/')
            }
            if (sepIx < 0) {
                invalidRational(string)
            }
            try {
                return Rational(
                    string.substring(0, sepIx).toInt(),
                    string.substring(sepIx + 1).toInt()
                )
            } catch (e: NumberFormatException) {
                invalidRational(string)
            }
        }
    }
}

actual final class Range<T : Comparable<T>> actual constructor(
    actual val lower: T,
    actual val upper: T
) {
    init {
        require(lower <= upper) { "lower must be less than or equal to upper" }
    }

    actual fun getLower(): T = lower
    actual fun getUpper(): T = upper

    actual operator fun contains(value: T): Boolean {
        return value >= lower && value <= upper
    }

    actual operator fun contains(range: Range<T>): Boolean {
        return range.lower >= lower && range.upper <= upper
    }

    actual fun clamp(value: T): T {
        return when {
            value < lower -> lower
            value > upper -> upper
            else -> value
        }
    }

    actual fun intersect(range: Range<T>): Range<T> {
        val cmpLower = range.lower.compareTo(lower)
        val cmpUpper = range.upper.compareTo(upper)
        if (cmpLower <= 0 && cmpUpper >= 0) {
            return this
        }
        if (cmpLower >= 0 && cmpUpper <= 0) {
            return range
        }
        return create(
            if (cmpLower <= 0) lower else range.lower,
            if (cmpUpper >= 0) upper else range.upper
        )
    }

    actual fun intersect(lower: T, upper: T): Range<T> {
        val cmpLower = lower.compareTo(this.lower)
        val cmpUpper = upper.compareTo(this.upper)
        if (cmpLower <= 0 && cmpUpper >= 0) {
            return this
        }
        return create(
            if (cmpLower <= 0) this.lower else lower,
            if (cmpUpper >= 0) this.upper else upper
        )
    }

    actual fun extend(range: Range<T>): Range<T> {
        val cmpLower = range.lower.compareTo(lower)
        val cmpUpper = range.upper.compareTo(upper)
        if (cmpLower <= 0 && cmpUpper >= 0) {
            return range
        }
        if (cmpLower >= 0 && cmpUpper <= 0) {
            return this
        }
        return create(
            if (cmpLower >= 0) lower else range.lower,
            if (cmpUpper <= 0) upper else range.upper
        )
    }

    actual fun extend(lower: T, upper: T): Range<T> {
        val cmpLower = lower.compareTo(this.lower)
        val cmpUpper = upper.compareTo(this.upper)
        if (cmpLower >= 0 && cmpUpper <= 0) {
            return this
        }
        return create(
            if (cmpLower >= 0) this.lower else lower,
            if (cmpUpper <= 0) this.upper else upper
        )
    }

    actual fun extend(value: T): Range<T> {
        return extend(value, value)
    }

    actual override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other is Range<*>) {
            return lower == other.lower && upper == other.upper
        }
        return false
    }

    actual override fun toString(): String = "[$lower, $upper]"

    actual override fun hashCode(): Int {
        val h1 = lower.hashCode()
        val h2 = upper.hashCode()
        return h2 xor ((h1 shl 16) or (h1 ushr 16))
    }

    actual companion object {
        actual fun <T : Comparable<T>> create(lower: T, upper: T): Range<T> = Range(lower, upper)
    }
}

actual class Base64 private constructor() {
    actual companion object {
        actual val DEFAULT: Int = 0
        actual val NO_PADDING: Int = 1
        actual val NO_WRAP: Int = 2
        actual val CRLF: Int = 4
        actual val URL_SAFE: Int = 8
        actual val NO_CLOSE: Int = 16

        private const val STD_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        private const val URL_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

        actual fun encodeToString(input: ByteArray, flags: Int): String {
            return String(encode(input, 0, input.size, flags), StandardCharsets.US_ASCII)
        }

        actual fun encodeToString(input: ByteArray, offset: Int, len: Int, flags: Int): String {
            return String(encode(input, offset, len, flags), StandardCharsets.US_ASCII)
        }

        actual fun encode(input: ByteArray, flags: Int): ByteArray {
            return encode(input, 0, input.size, flags)
        }

        actual fun encode(input: ByteArray, offset: Int, len: Int, flags: Int): ByteArray {
            if (offset < 0 || len < 0 || offset + len > input.size) {
                throw IllegalArgumentException("Invalid offset/len")
            }
            val alphabet = if ((flags and URL_SAFE) != 0) URL_ALPHABET else STD_ALPHABET
            val doPadding = (flags and NO_PADDING) == 0
            val doNewline = (flags and NO_WRAP) == 0
            val doCr = (flags and CRLF) != 0

            val out = ArrayList<Byte>(len * 4 / 3 + 16)
            var p = offset
            val end = offset + len
            var lineGroups = 19 // 19 * 4 = 76 chars per line

            while (p + 3 <= end) {
                val v = ((input[p].toInt() and 0xFF) shl 16) or
                    ((input[p + 1].toInt() and 0xFF) shl 8) or
                    (input[p + 2].toInt() and 0xFF)
                out.add(alphabet[(v ushr 18) and 0x3F].code.toByte())
                out.add(alphabet[(v ushr 12) and 0x3F].code.toByte())
                out.add(alphabet[(v ushr 6) and 0x3F].code.toByte())
                out.add(alphabet[v and 0x3F].code.toByte())
                p += 3
                lineGroups--
                if (doNewline && lineGroups == 0 && p < end) {
                    if (doCr) out.add('\r'.code.toByte())
                    out.add('\n'.code.toByte())
                    lineGroups = 19
                }
            }

            val rem = end - p
            if (rem == 1) {
                val v = (input[p].toInt() and 0xFF) shl 4
                out.add(alphabet[(v ushr 6) and 0x3F].code.toByte())
                out.add(alphabet[v and 0x3F].code.toByte())
                if (doPadding) {
                    out.add('='.code.toByte())
                    out.add('='.code.toByte())
                }
            } else if (rem == 2) {
                val v = ((input[p].toInt() and 0xFF) shl 10) or
                    ((input[p + 1].toInt() and 0xFF) shl 2)
                out.add(alphabet[(v ushr 12) and 0x3F].code.toByte())
                out.add(alphabet[(v ushr 6) and 0x3F].code.toByte())
                out.add(alphabet[v and 0x3F].code.toByte())
                if (doPadding) {
                    out.add('='.code.toByte())
                }
            }

            if (doNewline && (len > 0 || true)) {
                // Note: In AOSP Base64, when do_newline is true, even empty or exact-line inputs get a trailing newline
                if (doCr) out.add('\r'.code.toByte())
                out.add('\n'.code.toByte())
            }

            return out.toByteArray()
        }

        actual fun decode(str: String, flags: Int): ByteArray {
            val bytes = str.toByteArray(StandardCharsets.US_ASCII)
            return decode(bytes, 0, bytes.size, flags)
        }

        actual fun decode(input: ByteArray, flags: Int): ByteArray {
            return decode(input, 0, input.size, flags)
        }

        actual fun decode(input: ByteArray, offset: Int, len: Int, flags: Int): ByteArray {
            if (offset < 0 || len < 0 || offset + len > input.size) {
                throw IllegalArgumentException("Invalid offset/len")
            }
            val urlSafe = (flags and URL_SAFE) != 0
            val out = ArrayList<Byte>(len * 3 / 4)
            var state = 0
            var value = 0

            for (i in offset until (offset + len)) {
                val b = input[i].toInt() and 0xFF
                val d = decodeByte(b, urlSafe)
                if (d == SKIP) continue
                if (d == EQUALS) {
                    when (state) {
                        0, 1 -> throw IllegalArgumentException("bad base-64")
                        2 -> {
                            out.add((value shr 4).toByte())
                            state = 4
                        }
                        3 -> {
                            out.add((value shr 10).toByte())
                            out.add((value shr 2).toByte())
                            state = 5
                        }
                        4 -> state = 5
                        else -> throw IllegalArgumentException("bad base-64")
                    }
                    continue
                }
                if (d < 0) {
                    throw IllegalArgumentException("bad base-64")
                }
                when (state) {
                    0 -> {
                        value = d
                        state = 1
                    }
                    1 -> {
                        value = (value shl 6) or d
                        state = 2
                    }
                    2 -> {
                        value = (value shl 6) or d
                        state = 3
                    }
                    3 -> {
                        value = (value shl 6) or d
                        out.add((value shr 16).toByte())
                        out.add((value shr 8).toByte())
                        out.add(value.toByte())
                        state = 0
                    }
                    else -> throw IllegalArgumentException("bad base-64")
                }
            }

            when (state) {
                1, 4 -> throw IllegalArgumentException("bad base-64")
                2 -> out.add((value shr 4).toByte())
                3 -> {
                    out.add((value shr 10).toByte())
                    out.add((value shr 2).toByte())
                }
            }
            return out.toByteArray()
        }

        private const val SKIP = -1
        private const val EQUALS = -2
        private const val INVALID = -3

        private fun decodeByte(b: Int, urlSafe: Boolean): Int {
            return when (b.toChar()) {
                in 'A'..'Z' -> b - 'A'.code
                in 'a'..'z' -> b - 'a'.code + 26
                in '0'..'9' -> b - '0'.code + 52
                '+' -> if (urlSafe) INVALID else 62
                '/' -> if (urlSafe) INVALID else 63
                '-' -> if (urlSafe) 62 else INVALID
                '_' -> if (urlSafe) 63 else INVALID
                '=' -> EQUALS
                ' ', '\t', '\r', '\n' -> SKIP
                else -> INVALID
            }
        }
    }
}

actual class Patterns private constructor() {
    actual companion object {
        actual val IP_ADDRESS: Pattern = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]" +
                "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]" +
                "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}" +
                "|[1-9][0-9]|[0-9]))"
        )

        actual val DOMAIN_NAME: Pattern = Pattern.compile(
            "(([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,63}|" +
                IP_ADDRESS.pattern() + ")"
        )

        actual val WEB_URL: Pattern = Pattern.compile(
            "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)" +
                "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_" +
                "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?" +
                "(?:" + DOMAIN_NAME.pattern() + ")" +
                "(?:\\:\\d{1,5})?)" +
                "(\\/(?:(?:[a-zA-Z0-9;\\/\\?\\:\\@\\&\\=\\#\\~" +
                "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?" +
                "(?:\\b|$)"
        )

        actual val EMAIL_ADDRESS: Pattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
        )

        actual val PHONE: Pattern = Pattern.compile(
            "(\\+[0-9]+[\\- \\.]*)?" +
                "(\\([0-9]+\\)[\\- \\.]*)?" +
                "([0-9][0-9\\- \\.]+[0-9])"
        )
    }
}
