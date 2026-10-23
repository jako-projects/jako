@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.preference

import android.content.Context
import android.content.SharedPreferences

/**
 * [Nullability Decision] PreferenceManager.getDefaultSharedPreferences() returns a non-null SharedPreferences,
 * matching AOSP android.preference.PreferenceManager.
 */
expect open class PreferenceManager {
    companion object {
        fun getDefaultSharedPreferences(context: Context?): SharedPreferences
        fun getDefaultSharedPreferencesName(context: Context?): String
    }
}
