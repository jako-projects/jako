package android.util

import com.bennyhuo.jako.JakoApplication
import kotlin.math.abs

actual class DisplayMetrics actual constructor() {
    actual var widthPixels: Int = 0
    actual var heightPixels: Int = 0
    actual var density: Float = 1.0f
    actual var densityDpi: Int = DENSITY_DEFAULT
    actual var scaledDensity: Float = 1.0f
    actual var xdpi: Float = DENSITY_DEFAULT.toFloat()
    actual var ydpi: Float = DENSITY_DEFAULT.toFloat()

    init {
        setToDefaults()
    }

    actual fun setTo(o: DisplayMetrics) {
        if (this === o) return
        widthPixels = o.widthPixels
        heightPixels = o.heightPixels
        density = o.density
        densityDpi = o.densityDpi
        scaledDensity = o.scaledDensity
        xdpi = o.xdpi
        ydpi = o.ydpi
    }

    actual fun setToDefaults() {
        val cfg = JakoApplication.config
        widthPixels = cfg.screenWidthPx
        heightPixels = cfg.screenHeightPx
        densityDpi = cfg.densityDpi
        density = densityDpi / DENSITY_DEFAULT.toFloat()
        scaledDensity = density
        xdpi = densityDpi.toFloat()
        ydpi = densityDpi.toFloat()
    }

    actual override fun equals(other: Any?): Boolean = other is DisplayMetrics && equals(other)

    actual fun equals(other: DisplayMetrics?): Boolean {
        if (other == null) return false
        return widthPixels == other.widthPixels &&
            heightPixels == other.heightPixels &&
            density == other.density &&
            densityDpi == other.densityDpi &&
            scaledDensity == other.scaledDensity &&
            xdpi == other.xdpi &&
            ydpi == other.ydpi
    }

    actual override fun hashCode(): Int {
        return widthPixels * 31 + heightPixels * 17 + densityDpi
    }

    actual override fun toString(): String {
        return "DisplayMetrics{density=$density, width=$widthPixels, height=$heightPixels, scaledDensity=$scaledDensity, xdpi=$xdpi, ydpi=$ydpi}"
    }

    actual companion object {
        actual const val DENSITY_LOW: Int = 120
        actual const val DENSITY_MEDIUM: Int = 160
        actual const val DENSITY_TV: Int = 213
        actual const val DENSITY_HIGH: Int = 240
        actual const val DENSITY_260: Int = 260
        actual const val DENSITY_280: Int = 280
        actual const val DENSITY_300: Int = 300
        actual const val DENSITY_XHIGH: Int = 320
        actual const val DENSITY_340: Int = 340
        actual const val DENSITY_360: Int = 360
        actual const val DENSITY_400: Int = 400
        actual const val DENSITY_420: Int = 420
        actual const val DENSITY_440: Int = 440
        actual const val DENSITY_XXHIGH: Int = 480
        actual const val DENSITY_560: Int = 560
        actual const val DENSITY_600: Int = 600
        actual const val DENSITY_XXXHIGH: Int = 640
        actual const val DENSITY_DEFAULT: Int = DENSITY_MEDIUM
        actual const val DENSITY_DEVICE_STABLE: Int = DENSITY_MEDIUM
    }
}
