package java.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

actual open class IllegalFormatPrecisionException actual constructor(
    private val p: Int
) : IllegalFormatException() {
    actual open fun getPrecision(): Int = p
    actual open val precision: Int
        get() = p
    actual override val message: String?
        get() = p.toString()
}
