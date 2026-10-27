package android.preference

import android.content.Context
import android.content.SharedPreferences

/**
 * [Nullability Decision] PreferenceManager.getDefaultSharedPreferences() returns a non-null SharedPreferences,
 * matching AOSP android.preference.PreferenceManager.
 */
actual open class PreferenceManager {
    actual companion object {
        actual fun getDefaultSharedPreferences(context: Context?): SharedPreferences {
            val ctx = context!!
            return ctx.getSharedPreferences(
                getDefaultSharedPreferencesName(ctx),
                Context.MODE_PRIVATE
            )
        }

        actual fun getDefaultSharedPreferencesName(context: Context?): String {
            val ctx = context!!
            return "${ctx.getPackageName()}_preferences"
        }
    }
}
