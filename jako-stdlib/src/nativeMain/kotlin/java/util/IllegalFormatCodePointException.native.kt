package java.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

actual open class IllegalFormatCodePointException actual constructor(
    private val c: Int
) : IllegalFormatException() {
    actual open fun getCodePoint(): Int = c
    actual open val codePoint: Int
        get() = c
    actual override val message: String?
        get() = "Code point = 0x${c.toString(16)}"
}
