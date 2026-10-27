package java.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

actual open class MissingFormatArgumentException actual constructor(
    private val s: String
) : IllegalFormatException() {
    actual open fun getFormatSpecifier(): String = s
    actual open val formatSpecifier: String
        get() = s
    actual override val message: String?
        get() = "Format specifier '$s'"
}
