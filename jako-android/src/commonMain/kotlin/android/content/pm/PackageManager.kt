@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.content.pm

/**
 * Class for retrieving various kinds of information related to the application packages
 * that are currently installed on the device.
 */
expect abstract class PackageManager() {
    open class NameNotFoundException : Exception {
        constructor()
        constructor(name: String?)
    }

    abstract fun getPackageInfo(packageName: String, flags: Int): PackageInfo
    abstract fun getApplicationInfo(packageName: String, flags: Int): ApplicationInfo
    abstract fun getApplicationLabel(info: ApplicationInfo): CharSequence
    abstract fun checkPermission(permName: String, pkgName: String): Int
    abstract fun hasSystemFeature(name: String): Boolean
    abstract fun hasSystemFeature(name: String, version: Int): Boolean

    companion object {
        val GET_ACTIVITIES: Int
        val GET_RECEIVERS: Int
        val GET_SERVICES: Int
        val GET_PROVIDERS: Int
        val GET_META_DATA: Int
        val GET_SHARED_LIBRARY_FILES: Int
        val GET_PERMISSIONS: Int
        val MATCH_ALL: Int
        val MATCH_DEFAULT_ONLY: Int
        val MATCH_DISABLED_COMPONENTS: Int
        val MATCH_SYSTEM_ONLY: Int
        val MATCH_UNINSTALLED_PACKAGES: Int
        val PERMISSION_GRANTED: Int
        val PERMISSION_DENIED: Int
    }
}
