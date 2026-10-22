package java.util

import java.io.Closeable
import java.io.Flushable
import java.lang.Class
import java.lang.System
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

actual class Formatter : Closeable, Flushable {
    private val dest: Appendable
    private var closed: Boolean = false
    private val locale: Locale

    actual constructor() : this(StringBuilder(), Locale.getDefault())

    actual constructor(a: Appendable) : this(a, Locale.getDefault())

    actual constructor(l: Locale?) : this(StringBuilder(), l ?: Locale.getDefault())

    actual constructor(a: Appendable, l: Locale?) {
        this.dest = a
        this.locale = l ?: Locale.getDefault()
    }

    actual fun format(format: String, vararg args: Any?): Formatter {
        return format(this.locale, format, *args)
    }

    actual fun format(l: Locale?, format: String, vararg args: Any?): Formatter {
        ensureOpen()
        val effectiveLocale = l ?: this.locale
        dest.append(applyFormat(effectiveLocale, format, args))
        return this
    }

    actual fun locale(): Locale {
        ensureOpen()
        return locale
    }

    actual fun out(): Appendable {
        ensureOpen()
        return dest
    }

    actual override fun flush() {
        ensureOpen()
        (dest as? Flushable)?.flush()
    }

    actual override fun close() {
        if (!closed) {
            closed = true
            (dest as? Closeable)?.close()
        }
    }

    actual override fun toString(): String {
        ensureOpen()
        return dest.toString()
    }

    private fun ensureOpen() {
        if (closed) throw FormatterClosedException()
    }
}

internal fun applyFormat(locale: Locale, format: String, args: Array<out Any?>): String {
    val out = StringBuilder()
    var i = 0
    var nextArg = 0
    var lastArgIndex = -1
    while (i < format.length) {
        val ch = format[i]
        if (ch != '%') {
            out.append(ch)
            i++
            continue
        }
        val specStart = i
        i++
        if (i >= format.length) throw UnknownFormatConversionException("%")
        when (format[i]) {
            '%' -> {
                out.append('%')
                i++
                continue
            }
            'n' -> {
                out.append(System.getProperty("line.separator", "\n"))
                i++
                continue
            }
        }
        var explicitArgIndex = -1
        var useLastArg = false
        if (format[i] == '<') {
            useLastArg = true
            i++
        } else {
            var scanIdx = i
            while (scanIdx < format.length && format[scanIdx].isDigit()) scanIdx++
            if (scanIdx > i && scanIdx < format.length && format[scanIdx] == '$') {
                explicitArgIndex = format.substring(i, scanIdx).toInt() - 1
                i = scanIdx + 1
            }
        }
        var left = false
        var zeroPad = false
        var alternate = false
        while (i < format.length && format[i] in "-#+ 0,(") {
            when (format[i]) {
                '-' -> left = true
                '0' -> zeroPad = true
                '#' -> alternate = true
            }
            i++
        }
        var width = -1
        val widthStart = i
        while (i < format.length && format[i].isDigit()) i++
        if (i > widthStart) {
            width = format.substring(widthStart, i).toInt()
        }
        var precision = -1
        if (i < format.length && format[i] == '.') {
            i++
            val pStart = i
            while (i < format.length && format[i].isDigit()) i++
            precision = if (i > pStart) format.substring(pStart, i).toInt() else 0
        }
        if (i >= format.length) throw UnknownFormatConversionException("%")
        val conv = format[i]
        i++
        val specStr = format.substring(specStart, i)
        val argIndex = when {
            explicitArgIndex >= 0 -> {
                lastArgIndex = explicitArgIndex
                explicitArgIndex
            }
            useLastArg -> {
                if (lastArgIndex < 0) throw MissingFormatArgumentException(specStr)
                lastArgIndex
            }
            else -> {
                val idx = nextArg++
                lastArgIndex = idx
                idx
            }
        }
        if (argIndex < 0 || argIndex >= args.size) throw MissingFormatArgumentException(specStr)
        val arg = args[argIndex]
        if ((conv == 's' || conv == 'S') && arg is Formattable) {
            var flags = 0
            if (left) flags = flags or FormattableFlags.LEFT_JUSTIFY
            if (conv == 'S') flags = flags or FormattableFlags.UPPERCASE
            if (alternate) flags = flags or FormattableFlags.ALTERNATE
            val subOut = StringBuilder()
            val subFormatter = Formatter(subOut, locale)
            arg.formatTo(subFormatter, flags, width, precision)
            out.append(subOut)
            continue
        }
        var formatted = when (conv) {
            's' -> formatString(arg, precision, false)
            'S' -> formatString(arg, precision, true)
            'd' -> formatInteger(arg)
            'x' -> formatHex(arg, false)
            'X' -> formatHex(arg, true)
            'f' -> formatFixed(arg, if (precision >= 0) precision else 6)
            else -> throw UnknownFormatConversionException(conv.toString())
        }
        if (width > formatted.length) {
            if (zeroPad && !left) {
                if (formatted.startsWith("-")) {
                    val pad = "0".repeat(width - formatted.length)
                    formatted = "-" + pad + formatted.substring(1)
                } else if (formatted.startsWith("+")) {
                    val pad = "0".repeat(width - formatted.length)
                    formatted = "+" + pad + formatted.substring(1)
                } else {
                    val pad = "0".repeat(width - formatted.length)
                    formatted = pad + formatted
                }
            } else {
                val pad = " ".repeat(width - formatted.length)
                formatted = if (left) formatted + pad else pad + formatted
            }
        }
        out.append(formatted)
    }
    return out.toString()
}

internal fun formatString(arg: Any?, precision: Int, upper: Boolean): String {
    var s = arg?.toString() ?: "null"
    if (precision >= 0 && precision < s.length) s = s.substring(0, precision)
    return if (upper) s.uppercase() else s
}

@Suppress("UNCHECKED_CAST")
internal fun argClass(arg: Any?): Class<Any> =
    if (arg != null) Class<Any>(arg::class as kotlin.reflect.KClass<Any>) else Class<Any>(Any::class)

internal fun formatInteger(arg: Any?): String {
    val n = when (arg) {
        is Long -> arg
        is Int -> arg.toLong()
        is Short -> arg.toLong()
        is Byte -> arg.toLong()
        is Number -> arg.toLong()
        else -> throw IllegalFormatConversionException('d', argClass(arg))
    }
    return n.toString()
}

internal fun formatHex(arg: Any?, upper: Boolean): String {
    val hex = when (arg) {
        is Int -> arg.toUInt().toString(16)
        is Long -> arg.toULong().toString(16)
        is Short -> arg.toUShort().toString(16)
        is Byte -> arg.toUByte().toString(16)
        is Number -> arg.toLong().toULong().toString(16)
        else -> throw IllegalFormatConversionException(if (upper) 'X' else 'x', argClass(arg))
    }
    return if (upper) hex.uppercase() else hex
}

internal fun formatFixed(arg: Any?, precision: Int): String {
    val value = when (arg) {
        is Double -> arg
        is Float -> arg.toDouble()
        is Number -> arg.toDouble()
        else -> throw IllegalFormatConversionException('f', argClass(arg))
    }
    if (value.isNaN()) return "NaN"
    if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
    val negative = value < 0.0 || (value == 0.0 && 1.0 / value < 0.0)
    val absVal = abs(value)
    val factor = 10.0.pow(precision)
    val scaled = round(absVal * factor).toLong()
    var intPart = if (precision == 0) scaled else scaled / factor.toLong()
    var fracPart = if (precision == 0) 0L else scaled % factor.toLong()
    if (precision > 0 && fracPart < 0) {
        intPart -= 1
        fracPart += factor.toLong()
    }
    val fracStr = if (precision > 0) fracPart.toString().padStart(precision, '0') else ""
    return buildString {
        if (negative && (intPart != 0L || fracPart != 0L || value == 0.0)) {
            if (negative) append('-')
        } else if (negative) {
            append('-')
        }
        append(intPart)
        if (precision > 0) {
            append('.')
            append(fracStr)
        }
    }
}
