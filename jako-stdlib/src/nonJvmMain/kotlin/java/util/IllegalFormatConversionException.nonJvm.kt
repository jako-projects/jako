package java.util

import java.lang.Class
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

actual open class IllegalFormatConversionException actual constructor(
    private val c: Char,
    private val arg: Class<*>
) : IllegalFormatException() {
    actual open fun getConversion(): Char = c
    actual open val conversion: Char
        get() = c
    actual open fun getArgumentClass(): Class<*> = arg
    actual open val argumentClass: Class<*>
        get() = arg
    actual override val message: String?
        get() = "$c != ${arg.name}"
}
