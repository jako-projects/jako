package android.content.pm

abstract class PackageManager() {
    open class NameNotFoundException : Exception {
        constructor() : super()
        constructor(name: String?) : super(name)
    }

    abstract fun getPackageInfo(packageName: String, flags: Int): PackageInfo
    abstract fun getApplicationInfo(packageName: String, flags: Int): ApplicationInfo
    abstract fun getApplicationLabel(info: ApplicationInfo): CharSequence
    abstract fun checkPermission(permName: String, pkgName: String): Int
    abstract fun hasSystemFeature(name: String): Boolean
    abstract fun hasSystemFeature(name: String, version: Int): Boolean

    companion object {
        const val GET_ACTIVITIES: Int = 0x00000001
        const val GET_RECEIVERS: Int = 0x00000002
        const val GET_SERVICES: Int = 0x00000004
        const val GET_PROVIDERS: Int = 0x00000008
        const val GET_META_DATA: Int = 0x00000080
        const val GET_SHARED_LIBRARY_FILES: Int = 0x00000400
        const val GET_PERMISSIONS: Int = 0x00001000
        const val MATCH_ALL: Int = 0x00020000
        const val MATCH_DEFAULT_ONLY: Int = 0x00010000
        const val MATCH_DISABLED_COMPONENTS: Int = 0x00000200
        const val MATCH_SYSTEM_ONLY: Int = 0x00100000
        const val MATCH_UNINSTALLED_PACKAGES: Int = 0x00002000
        const val PERMISSION_GRANTED: Int = 0
        const val PERMISSION_DENIED: Int = -1
    }
}
