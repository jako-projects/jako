@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.content.pm

import android.os.Parcel
import android.os.Parcelable

/**
 * Information you can retrieve about a particular application.
 *
 * [Nullability Decision]:
 * - Directory and class/process name fields (`dataDir`, `sourceDir`, `nativeLibraryDir`, etc.) are nullable `String?`
 *   matching AOSP field declarations, and populated automatically when retrieved from `Context.getApplicationInfo()`.
 */
expect open class ApplicationInfo : PackageItemInfo, Parcelable {
    var taskAffinity: String?
    var permission: String?
    var processName: String?
    var className: String?
    var descriptionRes: Int
    var theme: Int
    var flags: Int
    var sourceDir: String?
    var publicSourceDir: String?
    var splitNames: Array<String>?
    var splitSourceDirs: Array<String>?
    var splitPublicSourceDirs: Array<String>?
    var sharedLibraryFiles: Array<String>?
    var dataDir: String?
    var deviceProtectedDataDir: String?
    var credentialProtectedDataDir: String?
    var nativeLibraryDir: String?
    var uid: Int
    var minSdkVersion: Int
    var targetSdkVersion: Int
    var enabled: Boolean

    constructor()
    constructor(orig: ApplicationInfo)

    override fun describeContents(): Int
    override fun writeToParcel(dest: Parcel, flags: Int)
    override fun toString(): String

    companion object {
        val FLAG_SYSTEM: Int
        val FLAG_DEBUGGABLE: Int
        val FLAG_HAS_CODE: Int
        val FLAG_PERSISTENT: Int
        val FLAG_FACTORY_TEST: Int
        val FLAG_ALLOW_TASK_REPARENTING: Int
        val FLAG_ALLOW_CLEAR_USER_DATA: Int
        val FLAG_UPDATED_SYSTEM_APP: Int
        val FLAG_TEST_ONLY: Int
        val FLAG_SUPPORTS_SMALL_SCREENS: Int
        val FLAG_SUPPORTS_NORMAL_SCREENS: Int
        val FLAG_SUPPORTS_LARGE_SCREENS: Int
        val FLAG_RESIZEABLE_FOR_SCREENS: Int
        val FLAG_SUPPORTS_SCREEN_DENSITIES: Int
        val FLAG_VM_SAFE_MODE: Int
        val FLAG_ALLOW_BACKUP: Int
        val FLAG_KILL_AFTER_RESTORE: Int
        val FLAG_RESTORE_ANY_VERSION: Int
        val FLAG_EXTERNAL_STORAGE: Int
        val FLAG_SUPPORTS_XLARGE_SCREENS: Int
        val FLAG_LARGE_HEAP: Int
        val FLAG_STOPPED: Int
        val FLAG_SUPPORTS_RTL: Int
        val FLAG_INSTALLED: Int
        val FLAG_IS_DATA_ONLY: Int
        val FLAG_EXTRACT_NATIVE_LIBS: Int
        val FLAG_HARDWARE_ACCELERATED: Int
        val FLAG_MULTIARCH: Int
    }
}
