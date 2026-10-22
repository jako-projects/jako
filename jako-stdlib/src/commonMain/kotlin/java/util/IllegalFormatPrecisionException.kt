@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class IllegalFormatPrecisionException(p: Int) : IllegalFormatException {
    open fun getPrecision(): Int
    open val precision: Int
    override val message: String?
}
