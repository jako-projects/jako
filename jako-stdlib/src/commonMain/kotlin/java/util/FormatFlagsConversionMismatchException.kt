@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class FormatFlagsConversionMismatchException(f: String, c: Char) : IllegalFormatException {
    open fun getFlags(): String
    open val flags: String
    open fun getConversion(): Char
    open val conversion: Char
    override val message: String?
}
