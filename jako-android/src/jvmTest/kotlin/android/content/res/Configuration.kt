package android.content.res

import android.os.Parcel
import android.os.Parcelable
import android.util.DisplayMetrics
import com.bennyhuo.jako.JakoApplication
import java.util.Locale
import kotlin.math.roundToInt

final class Configuration : Parcelable, Comparable<Configuration> {
    @JvmField var fontScale: Float = 1.0f
    @JvmField var mcc: Int = 0
    @JvmField var mnc: Int = 0
    @JvmField var locale: Locale? = Locale.getDefault()
    @JvmField var screenLayout: Int = SCREENLAYOUT_SIZE_NORMAL
    @JvmField var touchscreen: Int = 0
    @JvmField var keyboard: Int = 0
    @JvmField var keyboardHidden: Int = 0
    @JvmField var hardKeyboardHidden: Int = 0
    @JvmField var navigation: Int = 0
    @JvmField var navigationHidden: Int = 0
    @JvmField var orientation: Int = ORIENTATION_LANDSCAPE
    @JvmField var uiMode: Int = UI_MODE_TYPE_NORMAL or UI_MODE_NIGHT_NO
    @JvmField var screenWidthDp: Int = 1920
    @JvmField var screenHeightDp: Int = 1080
    @JvmField var smallestScreenWidthDp: Int = 1080
    @JvmField var densityDpi: Int = DisplayMetrics.DENSITY_DEFAULT

    constructor() {
        setToDefaults()
    }

    constructor(o: Configuration) {
        setTo(o)
    }

    fun setTo(o: Configuration) {
        fontScale = o.fontScale
        mcc = o.mcc
        mnc = o.mnc
        locale = o.locale
        screenLayout = o.screenLayout
        touchscreen = o.touchscreen
        keyboard = o.keyboard
        keyboardHidden = o.keyboardHidden
        hardKeyboardHidden = o.hardKeyboardHidden
        navigation = o.navigation
        navigationHidden = o.navigationHidden
        orientation = o.orientation
        uiMode = o.uiMode
        screenWidthDp = o.screenWidthDp
        screenHeightDp = o.screenHeightDp
        smallestScreenWidthDp = o.smallestScreenWidthDp
        densityDpi = o.densityDpi
    }

    fun setToDefaults() {
        val cfg = JakoApplication.config
        fontScale = 1.0f
        mcc = 0
        mnc = 0
        locale = cfg.locale
        screenLayout = SCREENLAYOUT_SIZE_NORMAL
        touchscreen = 0
        keyboard = 0
        keyboardHidden = 0
        hardKeyboardHidden = 0
        navigation = 0
        navigationHidden = 0
        densityDpi = cfg.densityDpi
        val density = if (densityDpi > 0) densityDpi / 160f else 1f
        screenWidthDp = (cfg.screenWidthPx / density).roundToInt()
        screenHeightDp = (cfg.screenHeightPx / density).roundToInt()
        smallestScreenWidthDp = minOf(screenWidthDp, screenHeightDp)
        orientation = if (cfg.screenWidthPx >= cfg.screenHeightPx) ORIENTATION_LANDSCAPE else ORIENTATION_PORTRAIT
        uiMode = UI_MODE_TYPE_NORMAL or UI_MODE_NIGHT_NO
    }

    fun updateFrom(delta: Configuration): Int {
        var changed = 0
        if (delta.fontScale > 0f && fontScale != delta.fontScale) {
            fontScale = delta.fontScale
            changed = changed or 0x40000000
        }
        if (delta.mcc != 0 && mcc != delta.mcc) {
            mcc = delta.mcc
            changed = changed or 0x0001
        }
        if (delta.mnc != 0 && mnc != delta.mnc) {
            mnc = delta.mnc
            changed = changed or 0x0002
        }
        if (delta.locale != null && locale != delta.locale) {
            locale = delta.locale
            changed = changed or 0x0004
        }
        if (delta.orientation != ORIENTATION_UNDEFINED && orientation != delta.orientation) {
            orientation = delta.orientation
            changed = changed or 0x0080
        }
        if (delta.densityDpi != DENSITY_DPI_UNDEFINED && densityDpi != delta.densityDpi) {
            densityDpi = delta.densityDpi
            changed = changed or 0x1000
        }
        if (delta.screenWidthDp != 0 && screenWidthDp != delta.screenWidthDp) {
            screenWidthDp = delta.screenWidthDp
            changed = changed or 0x0400
        }
        if (delta.screenHeightDp != 0 && screenHeightDp != delta.screenHeightDp) {
            screenHeightDp = delta.screenHeightDp
            changed = changed or 0x0400
        }
        return changed
    }

    fun diff(delta: Configuration): Int {
        var changed = 0
        if (delta.fontScale > 0f && fontScale != delta.fontScale) changed = changed or 0x40000000
        if (delta.mcc != 0 && mcc != delta.mcc) changed = changed or 0x0001
        if (delta.mnc != 0 && mnc != delta.mnc) changed = changed or 0x0002
        if (delta.locale != null && locale != delta.locale) changed = changed or 0x0004
        if (delta.orientation != ORIENTATION_UNDEFINED && orientation != delta.orientation) changed = changed or 0x0080
        if (delta.densityDpi != DENSITY_DPI_UNDEFINED && densityDpi != delta.densityDpi) changed = changed or 0x1000
        if (delta.screenWidthDp != 0 && screenWidthDp != delta.screenWidthDp) changed = changed or 0x0400
        if (delta.screenHeightDp != 0 && screenHeightDp != delta.screenHeightDp) changed = changed or 0x0400
        return changed
    }

    fun setLocale(loc: Locale?) {
        locale = loc
    }

    fun getLayoutDirection(): Int = 0

    fun isLayoutSizeAtLeast(size: Int): Boolean {
        val cur = screenLayout and SCREENLAYOUT_SIZE_MASK
        if (cur == SCREENLAYOUT_SIZE_UNDEFINED) return false
        return cur >= size
    }

    override fun compareTo(that: Configuration): Int {
        val n = fontScale.compareTo(that.fontScale)
        if (n != 0) return n
        if (mcc != that.mcc) return mcc - that.mcc
        if (mnc != that.mnc) return mnc - that.mnc
        val l1 = locale?.toString() ?: ""
        val l2 = that.locale?.toString() ?: ""
        val lc = l1.compareTo(l2)
        if (lc != 0) return lc
        if (orientation != that.orientation) return orientation - that.orientation
        if (densityDpi != that.densityDpi) return densityDpi - that.densityDpi
        if (screenWidthDp != that.screenWidthDp) return screenWidthDp - that.screenWidthDp
        return screenHeightDp - that.screenHeightDp
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is Configuration && equals(other)
    }

    fun equals(that: Configuration?): Boolean {
        if (that == null) return false
        if (this === that) return true
        return compareTo(that) == 0 &&
            screenLayout == that.screenLayout &&
            uiMode == that.uiMode &&
            smallestScreenWidthDp == that.smallestScreenWidthDp
    }

    override fun hashCode(): Int {
        var result = fontScale.hashCode()
        result = 31 * result + mcc
        result = 31 * result + mnc
        result = 31 * result + (locale?.hashCode() ?: 0)
        result = 31 * result + screenLayout
        result = 31 * result + orientation
        result = 31 * result + uiMode
        result = 31 * result + screenWidthDp
        result = 31 * result + screenHeightDp
        result = 31 * result + smallestScreenWidthDp
        result = 31 * result + densityDpi
        return result
    }

    override fun toString(): String {
        return "{${fontScale} ${mcc}mcc${mnc}mnc ${locale ?: ""} sw${smallestScreenWidthDp}dp w${screenWidthDp}dp h${screenHeightDp}dp ${densityDpi}dpi}"
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeFloat(fontScale)
        dest.writeInt(mcc)
        dest.writeInt(mnc)
        dest.writeString(locale?.toLanguageTag())
        dest.writeInt(screenLayout)
        dest.writeInt(orientation)
        dest.writeInt(uiMode)
        dest.writeInt(screenWidthDp)
        dest.writeInt(screenHeightDp)
        dest.writeInt(smallestScreenWidthDp)
        dest.writeInt(densityDpi)
    }

    fun readFromParcel(source: Parcel) {
        fontScale = source.readFloat()
        mcc = source.readInt()
        mnc = source.readInt()
        val tag = source.readString()
        locale = if (tag.isNullOrEmpty()) null else Locale.forLanguageTag(tag)
        screenLayout = source.readInt()
        orientation = source.readInt()
        uiMode = source.readInt()
        screenWidthDp = source.readInt()
        screenHeightDp = source.readInt()
        smallestScreenWidthDp = source.readInt()
        densityDpi = source.readInt()
    }

    companion object {
        const val ORIENTATION_UNDEFINED: Int = 0
        const val ORIENTATION_PORTRAIT: Int = 1
        const val ORIENTATION_LANDSCAPE: Int = 2
        const val ORIENTATION_SQUARE: Int = 3
        const val UI_MODE_TYPE_MASK: Int = 0x0f
        const val UI_MODE_TYPE_UNDEFINED: Int = 0x00
        const val UI_MODE_TYPE_NORMAL: Int = 0x01
        const val UI_MODE_TYPE_DESK: Int = 0x02
        const val UI_MODE_TYPE_CAR: Int = 0x03
        const val UI_MODE_TYPE_TELEVISION: Int = 0x04
        const val UI_MODE_TYPE_APPLIANCE: Int = 0x05
        const val UI_MODE_TYPE_WATCH: Int = 0x06
        const val UI_MODE_TYPE_VR_HEADSET: Int = 0x07
        const val UI_MODE_NIGHT_MASK: Int = 0x30
        const val UI_MODE_NIGHT_UNDEFINED: Int = 0x00
        const val UI_MODE_NIGHT_NO: Int = 0x10
        const val UI_MODE_NIGHT_YES: Int = 0x20
        const val SCREENLAYOUT_SIZE_MASK: Int = 0x0f
        const val SCREENLAYOUT_SIZE_UNDEFINED: Int = 0x00
        const val SCREENLAYOUT_SIZE_SMALL: Int = 0x01
        const val SCREENLAYOUT_SIZE_NORMAL: Int = 0x02
        const val SCREENLAYOUT_SIZE_LARGE: Int = 0x03
        const val SCREENLAYOUT_SIZE_XLARGE: Int = 0x04
        const val DENSITY_DPI_UNDEFINED: Int = 0
    }
}
