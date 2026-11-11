package android.graphics

import kotlin.math.abs

open class Color {
    companion object {
        const val BLACK: Int = -0x1000000
        const val DKGRAY: Int = -0xbbbbbc
        const val GRAY: Int = -0x777778
        const val LTGRAY: Int = -0x333334
        const val WHITE: Int = -0x1
        const val RED: Int = -0x10000
        const val GREEN: Int = -0xff0100
        const val BLUE: Int = -0xffff01
        const val YELLOW: Int = -0x100
        const val CYAN: Int = -0xff0001
        const val MAGENTA: Int = -0xff01
        const val TRANSPARENT: Int = 0

        private val sColorNameMap = mapOf(
            "black" to BLACK,
            "darkgray" to DKGRAY,
            "gray" to GRAY,
            "lightgray" to LTGRAY,
            "white" to WHITE,
            "red" to RED,
            "green" to GREEN,
            "blue" to BLUE,
            "yellow" to YELLOW,
            "cyan" to CYAN,
            "magenta" to MAGENTA,
            "aqua" to -0xff0001,
            "fuchsia" to -0xff01,
            "darkgrey" to DKGRAY,
            "grey" to GRAY,
            "lightgrey" to LTGRAY,
            "lime" to -0xff0100,
            "maroon" to -0x800000,
            "navy" to -0xffff80,
            "olive" to -0x7f8000,
            "purple" to -0x7fff80,
            "silver" to -0x3f3f40,
            "teal" to -0xff7f80
        )

        @JvmStatic
        fun alpha(color: Int): Int = color ushr 24

        @JvmStatic
        fun red(color: Int): Int = (color shr 16) and 0xFF

        @JvmStatic
        fun green(color: Int): Int = (color shr 8) and 0xFF

        @JvmStatic
        fun blue(color: Int): Int = color and 0xFF

        @JvmStatic
        fun rgb(red: Int, green: Int, blue: Int): Int {
            return (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
        }

        @JvmStatic
        fun rgb(red: Float, green: Float, blue: Float): Int {
            return (0xFF shl 24) or
                (((red * 255.0f + 0.5f).toInt()) shl 16) or
                (((green * 255.0f + 0.5f).toInt()) shl 8) or
                (blue * 255.0f + 0.5f).toInt()
        }

        @JvmStatic
        fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int {
            return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
        }

        @JvmStatic
        fun argb(alpha: Float, red: Float, green: Float, blue: Float): Int {
            return (((alpha * 255.0f + 0.5f).toInt()) shl 24) or
                (((red * 255.0f + 0.5f).toInt()) shl 16) or
                (((green * 255.0f + 0.5f).toInt()) shl 8) or
                (blue * 255.0f + 0.5f).toInt()
        }

        @JvmStatic
        fun parseColor(colorString: String): Int {
            if (colorString.isEmpty()) throw IllegalArgumentException("Unknown color")
            if (colorString[0] == '#') {
                var color = colorString.substring(1).toLongOrNull(16)
                    ?: throw IllegalArgumentException("Unknown color")
                if (colorString.length == 7) {
                    color = color or -0x1000000L
                } else if (colorString.length != 9) {
                    throw IllegalArgumentException("Unknown color")
                }
                return color.toInt()
            } else {
                val color = sColorNameMap[colorString.lowercase()]
                if (color != null) return color
            }
            throw IllegalArgumentException("Unknown color")
        }

        @JvmStatic
        fun RGBToHSV(red: Int, green: Int, blue: Int, hsv: FloatArray) {
            val rf = red / 255f
            val gf = green / 255f
            val bf = blue / 255f
            val max = maxOf(rf, gf, bf)
            val min = minOf(rf, gf, bf)
            val delta = max - min
            val v = max
            val s = if (max != 0f) delta / max else 0f
            var h = 0f
            if (s != 0f) {
                h = when (max) {
                    rf -> (gf - bf) / delta
                    gf -> 2f + (bf - rf) / delta
                    else -> 4f + (rf - gf) / delta
                }
                h *= 60f
                if (h < 0f) h += 360f
            }
            hsv[0] = h
            hsv[1] = s
            hsv[2] = v
        }

        @JvmStatic
        fun colorToHSV(color: Int, hsv: FloatArray) {
            RGBToHSV((color shr 16) and 0xFF, (color shr 8) and 0xFF, color and 0xFF, hsv)
        }

        @JvmStatic
        fun HSVToColor(hsv: FloatArray): Int = HSVToColor(0xFF, hsv)

        @JvmStatic
        fun HSVToColor(alpha: Int, hsv: FloatArray): Int {
            val h = ((hsv[0] % 360f) + 360f) % 360f
            val s = hsv[1].coerceIn(0f, 1f)
            val v = hsv[2].coerceIn(0f, 1f)
            val c = v * s
            val x = c * (1f - abs((h / 60f) % 2f - 1f))
            val m = v - c
            val (r1, g1, b1) = when {
                h < 60f -> Triple(c, x, 0f)
                h < 120f -> Triple(x, c, 0f)
                h < 180f -> Triple(0f, c, x)
                h < 240f -> Triple(0f, x, c)
                h < 300f -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }
            val r = ((r1 + m) * 255f + 0.5f).toInt().coerceIn(0, 255)
            val g = ((g1 + m) * 255f + 0.5f).toInt().coerceIn(0, 255)
            val b = ((b1 + m) * 255f + 0.5f).toInt().coerceIn(0, 255)
            return argb(alpha, r, g, b)
        }
    }
}
