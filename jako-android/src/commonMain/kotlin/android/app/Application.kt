@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.app

import android.content.ContextWrapper
import android.content.res.Configuration

/**
 * Base class for maintaining global application state.
 *
 * [Nullability Decision]:
 * - `getProcessName()` returns non-null `String` matching AOSP API 28+.
 * - `onConfigurationChanged(newConfig: Configuration)` takes non-null `Configuration`.
 */
expect open class Application() : ContextWrapper {
    open fun onCreate()
    open fun onTerminate()
    open fun onConfigurationChanged(newConfig: Configuration)
    open fun onLowMemory()
    open fun onTrimMemory(level: Int)

    companion object {
        fun getProcessName(): String
    }
}
