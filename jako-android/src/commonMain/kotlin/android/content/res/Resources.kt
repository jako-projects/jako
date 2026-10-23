@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.content.res

import android.util.DisplayMetrics
import android.util.TypedValue
import java.io.InputStream

/**
 * Class for accessing an application's resources.
 *
 * On PC / Native, [Resources] supports both:
 * 1. Automatic loading of XML values (`res/values/` XML files: `<string>`, `<integer>`, `<bool>`, `<color>`, `<dimen>`, `<string-array>`, `<integer-array>`)
 *    and raw files (`res/raw/` files) from the configured or discovered `res/` directory.
 * 2. Programmatic registration of resources by ID and name via `JakoApplication.init { ... }`.
 */
expect open class Resources(
    assets: AssetManager?,
    metrics: DisplayMetrics?,
    config: Configuration?
) {
    open class NotFoundException : RuntimeException {
        constructor()
        constructor(name: String?)
        constructor(name: String?, cause: Exception?)
    }

    val assets: AssetManager
    val displayMetrics: DisplayMetrics
    val configuration: Configuration

    fun getAssets(): AssetManager
    open fun getDisplayMetrics(): DisplayMetrics
    open fun getConfiguration(): Configuration

    open fun getString(id: Int): String
    open fun getString(id: Int, vararg formatArgs: Any?): String
    open fun getText(id: Int): CharSequence
    open fun getText(id: Int, def: CharSequence?): CharSequence?
    open fun getStringArray(id: Int): Array<String>
    open fun getIntArray(id: Int): IntArray
    open fun getInteger(id: Int): Int
    open fun getBoolean(id: Int): Boolean
    open fun getColor(id: Int): Int
    open fun getDimension(id: Int): Float
    open fun getDimensionPixelOffset(id: Int): Int
    open fun getDimensionPixelSize(id: Int): Int
    open fun openRawResource(id: Int): InputStream
    open fun openRawResource(id: Int, value: TypedValue?): InputStream
    open fun getValue(id: Int, outValue: TypedValue?, resolveRefs: Boolean)
    open fun getIdentifier(name: String?, defType: String?, defPackage: String?): Int
    open fun getResourceName(resid: Int): String
    open fun getResourcePackageName(resid: Int): String
    open fun getResourceTypeName(resid: Int): String
    open fun getResourceEntryName(resid: Int): String
    open fun updateConfiguration(config: Configuration?, metrics: DisplayMetrics?)

    companion object {
        fun getSystem(): Resources
    }
}
