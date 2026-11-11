package android.preference

import android.content.Context
import android.content.SharedPreferences

open class PreferenceManager {
    companion object {
        @JvmStatic
        fun getDefaultSharedPreferences(context: Context?): SharedPreferences {
            val ctx = context!!
            return ctx.getSharedPreferences(
                getDefaultSharedPreferencesName(ctx),
                Context.MODE_PRIVATE
            )
        }

        @JvmStatic
        fun getDefaultSharedPreferencesName(context: Context?): String {
            val ctx = context!!
            return "${ctx.getPackageName()}_preferences"
        }
    }
}
