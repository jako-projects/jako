package android.util

open class TypedValue() {
    @JvmField var type: Int = TYPE_NULL
    @JvmField var string: CharSequence? = null
    @JvmField var data: Int = 0
    @JvmField var assetCookie: Int = 0
    @JvmField var resourceId: Int = 0
    @JvmField var changingConfigurations: Int = -1
    @JvmField var density: Int = 0

    fun getFloat(): Float = Float.fromBits(data)

    fun getDimension(metrics: DisplayMetrics): Float =
        complexToDimension(data, metrics)

    fun getFraction(base: Float, pbase: Float): Float {
        val value = Float.fromBits(data)
        return value * base
    }

    fun coerceToString(): CharSequence? {
        val t = type
        if (t == TYPE_STRING) return string
        return when (t) {
            TYPE_NULL -> null
            TYPE_REFERENCE -> "@$data"
            TYPE_ATTRIBUTE -> "?$data"
            TYPE_FLOAT -> Float.fromBits(data).toString()
            TYPE_INT_DEC -> data.toString()
            TYPE_INT_HEX -> "0x${data.toUInt().toString(16)}"
            TYPE_INT_BOOLEAN -> if (data != 0) "true" else "false"
            else -> if (t in TYPE_INT_COLOR_ARGB8..TYPE_INT_COLOR_RGB4) {
                "#${data.toUInt().toString(16)}"
            } else {
                null
            }
        }
    }

    fun setTo(other: TypedValue) {
        type = other.type
        string = other.string
        data = other.data
        assetCookie = other.assetCookie
        resourceId = other.resourceId
        changingConfigurations = other.changingConfigurations
        density = other.density
    }

    override fun toString(): String {
        return "TypedValue{t=0x${type.toString(16)}/d=0x${data.toUInt().toString(16)} a=$assetCookie r=0x${resourceId.toUInt().toString(16)}}"
    }

    companion object {
        const val TYPE_NULL: Int = 0x00
        const val TYPE_REFERENCE: Int = 0x01
        const val TYPE_ATTRIBUTE: Int = 0x02
        const val TYPE_STRING: Int = 0x03
        const val TYPE_FLOAT: Int = 0x04
        const val TYPE_DIMENSION: Int = 0x05
        const val TYPE_FRACTION: Int = 0x06
        const val TYPE_INT_DEC: Int = 0x10
        const val TYPE_INT_HEX: Int = 0x11
        const val TYPE_INT_BOOLEAN: Int = 0x12
        const val TYPE_INT_COLOR_ARGB8: Int = 0x1c
        const val TYPE_INT_COLOR_RGB8: Int = 0x1d
        const val TYPE_INT_COLOR_ARGB4: Int = 0x1e
        const val TYPE_INT_COLOR_RGB4: Int = 0x1f

        const val COMPLEX_UNIT_PX: Int = 0
        const val COMPLEX_UNIT_DIP: Int = 1
        const val COMPLEX_UNIT_SP: Int = 2
        const val COMPLEX_UNIT_PT: Int = 3
        const val COMPLEX_UNIT_IN: Int = 4
        const val COMPLEX_UNIT_MM: Int = 5

        @JvmStatic
        fun applyDimension(unit: Int, value: Float, metrics: DisplayMetrics): Float {
            return when (unit) {
                COMPLEX_UNIT_PX -> value
                COMPLEX_UNIT_DIP -> value * metrics.density
                COMPLEX_UNIT_SP -> value * metrics.scaledDensity
                COMPLEX_UNIT_PT -> value * metrics.xdpi * (1.0f / 72)
                COMPLEX_UNIT_IN -> value * metrics.xdpi
                COMPLEX_UNIT_MM -> value * metrics.xdpi * (1.0f / 25.4f)
                else -> 0f
            }
        }

        @JvmStatic
        fun complexToDimension(data: Int, metrics: DisplayMetrics): Float {
            return Float.fromBits(data)
        }

        @JvmStatic
        fun complexToDimensionPixelOffset(data: Int, metrics: DisplayMetrics): Int {
            return complexToDimension(data, metrics).toInt()
        }

        @JvmStatic
        fun complexToDimensionPixelSize(data: Int, metrics: DisplayMetrics): Int {
            val f = complexToDimension(data, metrics)
            val res = (if (f >= 0f) f + 0.5f else f - 0.5f).toInt()
            if (res != 0) return res
            if (f == 0f) return 0
            return if (f > 0f) 1 else -1
        }
    }
}
