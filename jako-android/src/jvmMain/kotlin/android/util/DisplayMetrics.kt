package android.util

import com.bennyhuo.jako.JakoApplication

class DisplayMetrics() {
    @JvmField var widthPixels: Int = 0
    @JvmField var heightPixels: Int = 0
    @JvmField var density: Float = 1.0f
    @JvmField var densityDpi: Int = DENSITY_DEFAULT
    @JvmField var scaledDensity: Float = 1.0f
    @JvmField var xdpi: Float = DENSITY_DEFAULT.toFloat()
    @JvmField var ydpi: Float = DENSITY_DEFAULT.toFloat()

    init {
        setToDefaults()
    }

    fun setTo(o: DisplayMetrics) {
        if (this === o) return
        widthPixels = o.widthPixels
        heightPixels = o.heightPixels
        density = o.density
        densityDpi = o.densityDpi
        scaledDensity = o.scaledDensity
        xdpi = o.xdpi
        ydpi = o.ydpi
    }

    fun setToDefaults() {
        val cfg = JakoApplication.config
        widthPixels = cfg.screenWidthPx
        heightPixels = cfg.screenHeightPx
        densityDpi = cfg.densityDpi
        density = densityDpi / DENSITY_DEFAULT.toFloat()
        scaledDensity = density
        xdpi = densityDpi.toFloat()
        ydpi = densityDpi.toFloat()
    }

    override fun equals(other: Any?): Boolean = other is DisplayMetrics && equals(other)

    fun equals(other: DisplayMetrics?): Boolean {
        if (other == null) return false
        return widthPixels == other.widthPixels &&
            heightPixels == other.heightPixels &&
            density == other.density &&
            densityDpi == other.densityDpi &&
            scaledDensity == other.scaledDensity &&
            xdpi == other.xdpi &&
            ydpi == other.ydpi
    }

    override fun hashCode(): Int {
        return widthPixels * 31 + heightPixels * 17 + densityDpi
    }

    override fun toString(): String {
        return "DisplayMetrics{density=$density, width=$widthPixels, height=$heightPixels, scaledDensity=$scaledDensity, xdpi=$xdpi, ydpi=$ydpi}"
    }

    companion object {
        const val DENSITY_LOW: Int = 120
        const val DENSITY_MEDIUM: Int = 160
        const val DENSITY_TV: Int = 213
        const val DENSITY_HIGH: Int = 240
        const val DENSITY_260: Int = 260
        const val DENSITY_280: Int = 280
        const val DENSITY_300: Int = 300
        const val DENSITY_XHIGH: Int = 320
        const val DENSITY_340: Int = 340
        const val DENSITY_360: Int = 360
        const val DENSITY_400: Int = 400
        const val DENSITY_420: Int = 420
        const val DENSITY_440: Int = 440
        const val DENSITY_XXHIGH: Int = 480
        const val DENSITY_560: Int = 560
        const val DENSITY_600: Int = 600
        const val DENSITY_XXXHIGH: Int = 640
        const val DENSITY_DEFAULT: Int = DENSITY_MEDIUM
        const val DENSITY_DEVICE_STABLE: Int = DENSITY_MEDIUM
    }
}
