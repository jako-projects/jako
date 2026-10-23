package android.content.pm

actual abstract class PackageManager actual constructor() {
    actual open class NameNotFoundException : Exception {
        actual constructor() : super()
        actual constructor(name: String?) : super(name)
    }

    actual abstract fun getPackageInfo(packageName: String, flags: Int): PackageInfo
    actual abstract fun getApplicationInfo(packageName: String, flags: Int): ApplicationInfo
    actual abstract fun getApplicationLabel(info: ApplicationInfo): CharSequence
    actual abstract fun checkPermission(permName: String, pkgName: String): Int
    actual abstract fun hasSystemFeature(name: String): Boolean
    actual abstract fun hasSystemFeature(name: String, version: Int): Boolean

    actual companion object {
        actual const val GET_ACTIVITIES: Int = 0x00000001
        actual const val GET_RECEIVERS: Int = 0x00000002
        actual const val GET_SERVICES: Int = 0x00000004
        actual const val GET_PROVIDERS: Int = 0x00000008
        actual const val GET_META_DATA: Int = 0x00000080
        actual const val GET_SHARED_LIBRARY_FILES: Int = 0x00000400
        actual const val GET_PERMISSIONS: Int = 0x00001000
        actual const val MATCH_ALL: Int = 0x00020000
        actual const val MATCH_DEFAULT_ONLY: Int = 0x00010000
        actual const val MATCH_DISABLED_COMPONENTS: Int = 0x00000200
        actual const val MATCH_SYSTEM_ONLY: Int = 0x00100000
        actual const val MATCH_UNINSTALLED_PACKAGES: Int = 0x00002000
        actual const val PERMISSION_GRANTED: Int = 0
        actual const val PERMISSION_DENIED: Int = -1
    }
}
