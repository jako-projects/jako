@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.content.res

import android.os.Parcel
import android.os.Parcelable
import java.util.Locale

/**
 * This class describes all device configuration information that can impact the resources
 * the application retrieves.
 */
expect final class Configuration : Parcelable, Comparable<Configuration> {
    var fontScale: Float
    var mcc: Int
    var mnc: Int
    var locale: Locale?
    var screenLayout: Int
    var touchscreen: Int
    var keyboard: Int
    var keyboardHidden: Int
    var hardKeyboardHidden: Int
    var navigation: Int
    var navigationHidden: Int
    var orientation: Int
    var uiMode: Int
    var screenWidthDp: Int
    var screenHeightDp: Int
    var smallestScreenWidthDp: Int
    var densityDpi: Int

    constructor()
    constructor(o: Configuration)

    fun setTo(o: Configuration)
    fun setToDefaults()
    fun updateFrom(delta: Configuration): Int
    fun diff(delta: Configuration): Int
    fun setLocale(loc: Locale?)
    fun getLayoutDirection(): Int
    fun isLayoutSizeAtLeast(size: Int): Boolean
    override fun compareTo(that: Configuration): Int
    override fun equals(other: Any?): Boolean
    fun equals(that: Configuration?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
    override fun describeContents(): Int
    override fun writeToParcel(dest: Parcel, flags: Int)
    fun readFromParcel(source: Parcel)

    companion object {
        val ORIENTATION_UNDEFINED: Int
        val ORIENTATION_PORTRAIT: Int
        val ORIENTATION_LANDSCAPE: Int
        val ORIENTATION_SQUARE: Int
        val UI_MODE_TYPE_MASK: Int
        val UI_MODE_TYPE_UNDEFINED: Int
        val UI_MODE_TYPE_NORMAL: Int
        val UI_MODE_TYPE_DESK: Int
        val UI_MODE_TYPE_CAR: Int
        val UI_MODE_TYPE_TELEVISION: Int
        val UI_MODE_TYPE_APPLIANCE: Int
        val UI_MODE_TYPE_WATCH: Int
        val UI_MODE_TYPE_VR_HEADSET: Int
        val UI_MODE_NIGHT_MASK: Int
        val UI_MODE_NIGHT_UNDEFINED: Int
        val UI_MODE_NIGHT_NO: Int
        val UI_MODE_NIGHT_YES: Int
        val SCREENLAYOUT_SIZE_MASK: Int
        val SCREENLAYOUT_SIZE_UNDEFINED: Int
        val SCREENLAYOUT_SIZE_SMALL: Int
        val SCREENLAYOUT_SIZE_NORMAL: Int
        val SCREENLAYOUT_SIZE_LARGE: Int
        val SCREENLAYOUT_SIZE_XLARGE: Int
        val DENSITY_DPI_UNDEFINED: Int
    }
}
