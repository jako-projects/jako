package java.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

actual open class FormatFlagsConversionMismatchException actual constructor(
    private val f: String,
    private val c: Char
) : IllegalFormatException() {
    actual open fun getFlags(): String = f
    actual open val flags: String
        get() = f
    actual open fun getConversion(): Char = c
    actual open val conversion: Char
        get() = c
    actual override val message: String?
        get() = "Conversion = $c, Flags = $f"
}
