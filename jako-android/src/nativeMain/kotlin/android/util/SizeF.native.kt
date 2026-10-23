package android.util

import kotlin.math.abs

actual final class SizeF actual constructor(
    actual val width: Float,
    actual val height: Float
) {
    init {
        require(!width.isNaN() && !width.isInfinite()) { "width must be finite" }
        require(!height.isNaN() && !height.isInfinite()) { "height must be finite" }
    }

    actual fun getWidth(): Float = width
    actual fun getHeight(): Float = height

    actual override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other is SizeF) {
            return width == other.width && height == other.height
        }
        return false
    }

    actual override fun toString(): String = "${width}x${height}"

    actual override fun hashCode(): Int {
        return width.toBits() xor height.toBits()
    }

    actual companion object {
        private fun invalidSizeF(s: String): Nothing {
            throw NumberFormatException("Invalid SizeF: \"$s\"")
        }

        actual fun parseSizeF(string: String): SizeF {
            var sepIx = string.indexOf('*')
            if (sepIx < 0) {
                sepIx = string.indexOf('x')
            }
            if (sepIx < 0) {
                invalidSizeF(string)
            }
            try {
                return SizeF(
                    string.substring(0, sepIx).toFloat(),
                    string.substring(sepIx + 1).toFloat()
                )
            } catch (e: NumberFormatException) {
                invalidSizeF(string)
            } catch (e: IllegalArgumentException) {
                invalidSizeF(string)
            }
        }
    }
}
