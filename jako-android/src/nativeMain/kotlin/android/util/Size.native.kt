package android.util

import kotlin.math.abs

actual final class Size actual constructor(
    actual val width: Int,
    actual val height: Int
) {
    actual fun getWidth(): Int = width
    actual fun getHeight(): Int = height

    actual override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other is Size) {
            return width == other.width && height == other.height
        }
        return false
    }

    actual override fun toString(): String = "${width}x${height}"

    actual override fun hashCode(): Int {
        return height xor ((width shl (Int.SIZE_BITS / 2)) or (width ushr (Int.SIZE_BITS / 2)))
    }

    actual companion object {
        private fun invalidSize(s: String): Nothing {
            throw NumberFormatException("Invalid Size: \"$s\"")
        }

        actual fun parseSize(string: String): Size {
            var sepIx = string.indexOf('*')
            if (sepIx < 0) {
                sepIx = string.indexOf('x')
            }
            if (sepIx < 0) {
                invalidSize(string)
            }
            try {
                return Size(
                    string.substring(0, sepIx).toInt(),
                    string.substring(sepIx + 1).toInt()
                )
            } catch (e: NumberFormatException) {
                invalidSize(string)
            }
        }
    }
}
