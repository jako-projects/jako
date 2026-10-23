package android.os

/**
 * Desktop JVM shim for hidden AOSP `android.os.SystemProperties`, which is invoked
 * by `android.os.Build.<clinit>` in `org.robolectric:android-all`.
 */
class SystemProperties {
    companion object {
        @JvmStatic
        fun get(key: String): String = ""

        @JvmStatic
        fun get(key: String, def: String?): String? = def

        @JvmStatic
        fun getInt(key: String, def: Int): Int = def

        @JvmStatic
        fun getLong(key: String, def: Long): Long = def

        @JvmStatic
        fun getBoolean(key: String, def: Boolean): Boolean = def
    }
}
