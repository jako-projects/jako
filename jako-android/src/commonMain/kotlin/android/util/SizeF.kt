@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

/**
 * Immutable class for describing width and height dimensions in floating-point values.
 */
expect final class SizeF(width: Float, height: Float) {
    val width: Float
    val height: Float

    fun getWidth(): Float
    fun getHeight(): Float
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        fun parseSizeF(string: String): SizeF
    }
}
