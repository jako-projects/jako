package java.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

actual open class UnknownFormatConversionException actual constructor(
    private val s: String
) : IllegalFormatException() {
    actual open fun getConversion(): String = s
    actual open val conversion: String
        get() = s
    actual override val message: String?
        get() = "Conversion = '$s'"
}
