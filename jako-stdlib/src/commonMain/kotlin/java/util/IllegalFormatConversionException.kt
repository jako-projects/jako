@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.lang.Class

expect open class IllegalFormatConversionException(c: Char, arg: Class<*>) : IllegalFormatException {
    open fun getConversion(): Char
    open val conversion: Char
    open fun getArgumentClass(): Class<*>
    open val argumentClass: Class<*>
    override val message: String?
}
