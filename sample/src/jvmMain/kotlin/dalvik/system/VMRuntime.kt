package dalvik.system

import java.lang.reflect.Array as JArray

/**
 * Desktop JVM shim for `dalvik.system.VMRuntime` when running `sample` against
 * `org.robolectric:android-all` on standard OpenJDK without an Android runtime.
 */
class VMRuntime private constructor() {
    fun newUnpaddedArray(componentType: Class<*>, minLength: Int): Any {
        return JArray.newInstance(componentType, minLength)
    }

    fun newNonMovableArray(componentType: Class<*>, length: Int): Any {
        return JArray.newInstance(componentType, length)
    }

    fun getTargetSdkVersion(): Int = 29

    companion object {
        private val THE_ONE = VMRuntime()

        @JvmStatic
        fun getRuntime(): VMRuntime = THE_ONE
    }
}
