@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

/**
 * Container for a dynamically typed data value. Primarily used with [android.content.res.Resources]
 * for holding resource values.
 */
expect open class TypedValue() {
    var type: Int
    var string: CharSequence?
    var data: Int
    var assetCookie: Int
    var resourceId: Int
    var changingConfigurations: Int
    var density: Int

    fun getFloat(): Float
    fun getDimension(metrics: DisplayMetrics): Float
    fun getFraction(base: Float, pbase: Float): Float
    fun coerceToString(): CharSequence?
    fun setTo(other: TypedValue)
    override fun toString(): String

    companion object {
        val TYPE_NULL: Int
        val TYPE_REFERENCE: Int
        val TYPE_ATTRIBUTE: Int
        val TYPE_STRING: Int
        val TYPE_FLOAT: Int
        val TYPE_DIMENSION: Int
        val TYPE_FRACTION: Int
        val TYPE_INT_DEC: Int
        val TYPE_INT_HEX: Int
        val TYPE_INT_BOOLEAN: Int
        val TYPE_INT_COLOR_ARGB8: Int
        val TYPE_INT_COLOR_RGB8: Int
        val TYPE_INT_COLOR_ARGB4: Int
        val TYPE_INT_COLOR_RGB4: Int

        val COMPLEX_UNIT_PX: Int
        val COMPLEX_UNIT_DIP: Int
        val COMPLEX_UNIT_SP: Int
        val COMPLEX_UNIT_PT: Int
        val COMPLEX_UNIT_IN: Int
        val COMPLEX_UNIT_MM: Int

        fun applyDimension(unit: Int, value: Float, metrics: DisplayMetrics): Float
        fun complexToDimension(data: Int, metrics: DisplayMetrics): Float
        fun complexToDimensionPixelOffset(data: Int, metrics: DisplayMetrics): Int
        fun complexToDimensionPixelSize(data: Int, metrics: DisplayMetrics): Int
    }
}
