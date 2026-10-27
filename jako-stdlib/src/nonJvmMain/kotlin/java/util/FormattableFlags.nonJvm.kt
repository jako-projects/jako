package java.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

actual class FormattableFlags private constructor() {
    actual companion object {
        actual val LEFT_JUSTIFY: Int = 1
        actual val UPPERCASE: Int = 2
        actual val ALTERNATE: Int = 4
    }
}
