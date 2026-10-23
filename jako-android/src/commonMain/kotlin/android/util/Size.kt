@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

/**
 * Immutable class for describing width and height dimensions in pixels.
 *
 * [Nullability Decision]:
 * - `parseSize(string: String)`: non-null `String` parameter, throws `NumberFormatException` on invalid format.
 */
expect final class Size(width: Int, height: Int) {
    val width: Int
    val height: Int

    fun getWidth(): Int
    fun getHeight(): Int
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        fun parseSize(string: String): Size
    }
}
