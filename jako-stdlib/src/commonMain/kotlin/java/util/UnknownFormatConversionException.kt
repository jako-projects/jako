@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class UnknownFormatConversionException(s: String) : IllegalFormatException {
    open fun getConversion(): String
    open val conversion: String
    override val message: String?
}
