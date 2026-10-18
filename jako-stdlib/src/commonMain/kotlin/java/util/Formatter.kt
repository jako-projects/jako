@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Closeable
import java.io.Flushable
import java.lang.Class

expect class Formatter : Closeable, Flushable {
    constructor()
    // [Nullability Decision] Java Formatter(Appendable) destination is non-null; null throws NPE.
    constructor(a: Appendable)
    constructor(l: Locale?)
    constructor(a: Appendable, l: Locale?)
    fun format(format: String, vararg args: Any?): Formatter
    fun format(l: Locale?, format: String, vararg args: Any?): Formatter
    fun locale(): Locale
    fun out(): Appendable
    override fun flush()
    override fun close()
    override fun toString(): String
}

expect fun interface Formattable {
    fun formatTo(formatter: Formatter, flags: Int, width: Int, precision: Int)
}

expect class FormattableFlags {
    companion object {
        val LEFT_JUSTIFY: Int
        val UPPERCASE: Int
        val ALTERNATE: Int
    }
}

expect open class FormatterClosedException() : IllegalStateException

expect open class IllegalFormatException : IllegalArgumentException

expect open class MissingFormatArgumentException(s: String) : IllegalFormatException {
    open fun getFormatSpecifier(): String
    open val formatSpecifier: String
    override val message: String?
}

expect open class UnknownFormatConversionException(s: String) : IllegalFormatException {
    open fun getConversion(): String
    open val conversion: String
    override val message: String?
}

expect open class DuplicateFormatFlagsException(f: String) : IllegalFormatException {
    open fun getFlags(): String
    open val flags: String
    override val message: String?
}

expect open class FormatFlagsConversionMismatchException(f: String, c: Char) : IllegalFormatException {
    open fun getFlags(): String
    open val flags: String
    open fun getConversion(): Char
    open val conversion: Char
    override val message: String?
}

expect open class IllegalFormatCodePointException(c: Int) : IllegalFormatException {
    open fun getCodePoint(): Int
    open val codePoint: Int
    override val message: String?
}

expect open class IllegalFormatConversionException(c: Char, arg: Class<*>) : IllegalFormatException {
    open fun getConversion(): Char
    open val conversion: Char
    open fun getArgumentClass(): Class<*>
    open val argumentClass: Class<*>
    override val message: String?
}

expect open class IllegalFormatFlagsException(f: String) : IllegalFormatException {
    open fun getFlags(): String
    open val flags: String
    override val message: String?
}

expect open class IllegalFormatPrecisionException(p: Int) : IllegalFormatException {
    open fun getPrecision(): Int
    open val precision: Int
    override val message: String?
}

expect open class IllegalFormatWidthException(w: Int) : IllegalFormatException {
    open fun getWidth(): Int
    open val width: Int
    override val message: String?
}

expect open class MissingFormatWidthException(s: String) : IllegalFormatException {
    open fun getFormatSpecifier(): String
    open val formatSpecifier: String
    override val message: String?
}
