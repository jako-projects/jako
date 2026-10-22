package java.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

actual open class DuplicateFormatFlagsException actual constructor(
    private val f: String
) : IllegalFormatException() {
    actual open fun getFlags(): String = f
    actual open val flags: String
        get() = f
    actual override val message: String?
        get() = "Flags = '$f'"
}
