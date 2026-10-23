package android.content.pm

import android.os.Parcel
import android.os.Parcelable

actual open class ApplicationInfo : PackageItemInfo, Parcelable {
    actual var taskAffinity: String? = null
    actual var permission: String? = null
    actual var processName: String? = null
    actual var className: String? = null
    actual var descriptionRes: Int = 0
    actual var theme: Int = 0
    actual var flags: Int = 0
    actual var sourceDir: String? = null
    actual var publicSourceDir: String? = null
    actual var splitNames: Array<String>? = null
    actual var splitSourceDirs: Array<String>? = null
    actual var splitPublicSourceDirs: Array<String>? = null
    actual var sharedLibraryFiles: Array<String>? = null
    actual var dataDir: String? = null
    actual var deviceProtectedDataDir: String? = null
    actual var credentialProtectedDataDir: String? = null
    actual var nativeLibraryDir: String? = null
    actual var uid: Int = 0
    actual var minSdkVersion: Int = 21
    actual var targetSdkVersion: Int = 29
    actual var enabled: Boolean = true

    actual constructor() : super()

    actual constructor(orig: ApplicationInfo) : super(orig) {
        taskAffinity = orig.taskAffinity
        permission = orig.permission
        processName = orig.processName
        className = orig.className
        descriptionRes = orig.descriptionRes
        theme = orig.theme
        flags = orig.flags
        sourceDir = orig.sourceDir
        publicSourceDir = orig.publicSourceDir
        splitNames = orig.splitNames?.copyOf()
        splitSourceDirs = orig.splitSourceDirs?.copyOf()
        splitPublicSourceDirs = orig.splitPublicSourceDirs?.copyOf()
        sharedLibraryFiles = orig.sharedLibraryFiles?.copyOf()
        dataDir = orig.dataDir
        deviceProtectedDataDir = orig.deviceProtectedDataDir
        credentialProtectedDataDir = orig.credentialProtectedDataDir
        nativeLibraryDir = orig.nativeLibraryDir
        uid = orig.uid
        minSdkVersion = orig.minSdkVersion
        targetSdkVersion = orig.targetSdkVersion
        enabled = orig.enabled
    }

    actual override fun describeContents(): Int = 0

    actual override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(packageName)
        dest.writeString(processName)
        dest.writeString(dataDir)
        dest.writeString(sourceDir)
        dest.writeString(nativeLibraryDir)
        dest.writeInt(this.flags)
        dest.writeInt(uid)
        dest.writeInt(minSdkVersion)
        dest.writeInt(targetSdkVersion)
        dest.writeInt(if (enabled) 1 else 0)
        dest.writeBundle(metaData)
    }

    actual override fun toString(): String =
        "ApplicationInfo{${hashCode().toString(16)} ${packageName ?: "unknown"}}"

    actual companion object {
        actual const val FLAG_SYSTEM: Int = 1 shl 0
        actual const val FLAG_DEBUGGABLE: Int = 1 shl 1
        actual const val FLAG_HAS_CODE: Int = 1 shl 2
        actual const val FLAG_PERSISTENT: Int = 1 shl 3
        actual const val FLAG_FACTORY_TEST: Int = 1 shl 4
        actual const val FLAG_ALLOW_TASK_REPARENTING: Int = 1 shl 5
        actual const val FLAG_ALLOW_CLEAR_USER_DATA: Int = 1 shl 6
        actual const val FLAG_UPDATED_SYSTEM_APP: Int = 1 shl 7
        actual const val FLAG_TEST_ONLY: Int = 1 shl 8
        actual const val FLAG_SUPPORTS_SMALL_SCREENS: Int = 1 shl 9
        actual const val FLAG_SUPPORTS_NORMAL_SCREENS: Int = 1 shl 10
        actual const val FLAG_SUPPORTS_LARGE_SCREENS: Int = 1 shl 11
        actual const val FLAG_RESIZEABLE_FOR_SCREENS: Int = 1 shl 12
        actual const val FLAG_SUPPORTS_SCREEN_DENSITIES: Int = 1 shl 13
        actual const val FLAG_VM_SAFE_MODE: Int = 1 shl 14
        actual const val FLAG_ALLOW_BACKUP: Int = 1 shl 15
        actual const val FLAG_KILL_AFTER_RESTORE: Int = 1 shl 16
        actual const val FLAG_RESTORE_ANY_VERSION: Int = 1 shl 17
        actual const val FLAG_EXTERNAL_STORAGE: Int = 1 shl 18
        actual const val FLAG_SUPPORTS_XLARGE_SCREENS: Int = 1 shl 19
        actual const val FLAG_LARGE_HEAP: Int = 1 shl 20
        actual const val FLAG_STOPPED: Int = 1 shl 21
        actual const val FLAG_SUPPORTS_RTL: Int = 1 shl 22
        actual const val FLAG_INSTALLED: Int = 1 shl 23
        actual const val FLAG_IS_DATA_ONLY: Int = 1 shl 24
        actual const val FLAG_EXTRACT_NATIVE_LIBS: Int = 1 shl 28
        actual const val FLAG_HARDWARE_ACCELERATED: Int = 1 shl 29
        actual const val FLAG_MULTIARCH: Int = 1 shl 31
    }
}
