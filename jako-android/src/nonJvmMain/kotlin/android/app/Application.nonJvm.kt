package android.app

import android.content.ContextWrapper
import android.content.res.Configuration
import com.bennyhuo.jako.JakoApplication

/**
 * Base class for maintaining global application state.
 */
actual open class Application actual constructor() : ContextWrapper(null) {
    actual open fun onCreate() {}

    actual open fun onTerminate() {}

    actual open fun onConfigurationChanged(newConfig: Configuration) {}

    actual open fun onLowMemory() {}

    actual open fun onTrimMemory(level: Int) {}

    actual companion object {
        actual fun getProcessName(): String = JakoApplication.config.packageName
    }
}
