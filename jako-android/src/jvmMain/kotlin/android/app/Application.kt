package android.app

import android.content.ContextWrapper
import android.content.res.Configuration
import com.bennyhuo.jako.JakoApplication

open class Application() : ContextWrapper(null) {
    open fun onCreate() {}

    open fun onTerminate() {}

    open fun onConfigurationChanged(newConfig: Configuration) {}

    open fun onLowMemory() {}

    open fun onTrimMemory(level: Int) {}

    companion object {
        @JvmStatic
        fun getProcessName(): String = JakoApplication.config.packageName
    }
}
