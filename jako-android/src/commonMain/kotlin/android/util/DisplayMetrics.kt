@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

/**
 * A structure describing general information about a display, such as its size, density,
 * and font scaling.
 */
expect class DisplayMetrics() {
    var widthPixels: Int
    var heightPixels: Int
    var density: Float
    var densityDpi: Int
    var scaledDensity: Float
    var xdpi: Float
    var ydpi: Float

    fun setTo(o: DisplayMetrics)
    fun setToDefaults()
    override fun equals(other: Any?): Boolean
    fun equals(other: DisplayMetrics?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        val DENSITY_LOW: Int
        val DENSITY_MEDIUM: Int
        val DENSITY_TV: Int
        val DENSITY_HIGH: Int
        val DENSITY_260: Int
        val DENSITY_280: Int
        val DENSITY_300: Int
        val DENSITY_XHIGH: Int
        val DENSITY_340: Int
        val DENSITY_360: Int
        val DENSITY_400: Int
        val DENSITY_420: Int
        val DENSITY_440: Int
        val DENSITY_XXHIGH: Int
        val DENSITY_560: Int
        val DENSITY_600: Int
        val DENSITY_XXXHIGH: Int
        val DENSITY_DEFAULT: Int
        val DENSITY_DEVICE_STABLE: Int
    }
}
