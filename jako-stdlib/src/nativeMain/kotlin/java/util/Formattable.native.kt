package java.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

actual fun interface Formattable {
    actual fun formatTo(formatter: Formatter, flags: Int, width: Int, precision: Int)
}
