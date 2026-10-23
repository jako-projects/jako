@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.graphics

/**
 * The Color class defines methods for creating, converting and manipulating colors.
 */
expect open class Color() {
    companion object {
        val BLACK: Int
        val DKGRAY: Int
        val GRAY: Int
        val LTGRAY: Int
        val WHITE: Int
        val RED: Int
        val GREEN: Int
        val BLUE: Int
        val YELLOW: Int
        val CYAN: Int
        val MAGENTA: Int
        val TRANSPARENT: Int

        fun alpha(color: Int): Int
        fun red(color: Int): Int
        fun green(color: Int): Int
        fun blue(color: Int): Int
        fun rgb(red: Int, green: Int, blue: Int): Int
        fun rgb(red: Float, green: Float, blue: Float): Int
        fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int
        fun argb(alpha: Float, red: Float, green: Float, blue: Float): Int
        fun parseColor(colorString: String): Int
        fun RGBToHSV(red: Int, green: Int, blue: Int, hsv: FloatArray)
        fun colorToHSV(color: Int, hsv: FloatArray)
        fun HSVToColor(hsv: FloatArray): Int
        fun HSVToColor(alpha: Int, hsv: FloatArray): Int
    }
}
