package java.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

actual open class IllegalFormatWidthException actual constructor(
    private val w: Int
) : IllegalFormatException() {
    actual open fun getWidth(): Int = w
    actual open val width: Int
        get() = w
    actual override val message: String?
        get() = w.toString()
}
