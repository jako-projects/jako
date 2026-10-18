package dalvik.system

import java.lang.reflect.Array as JArray

/**
 * JVM test-only shim for `dalvik.system.VMRuntime` so that AOSP classes in
 * `org.robolectric:android-all` (such as `SparseArray`, `TextUtils`, `ArrayUtils`)
 * can allocate arrays on standard OpenJDK without JNI `UnsatisfiedLinkError`.
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
