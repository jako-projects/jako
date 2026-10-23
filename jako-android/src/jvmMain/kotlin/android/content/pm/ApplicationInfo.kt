package android.content.pm

import android.os.Parcel
import android.os.Parcelable

open class ApplicationInfo : PackageItemInfo, Parcelable {
    @JvmField var taskAffinity: String? = null
    @JvmField var permission: String? = null
    @JvmField var processName: String? = null
    @JvmField var className: String? = null
    @JvmField var descriptionRes: Int = 0
    @JvmField var theme: Int = 0
    @JvmField var flags: Int = 0
    @JvmField var sourceDir: String? = null
    @JvmField var publicSourceDir: String? = null
    @JvmField var splitNames: Array<String>? = null
    @JvmField var splitSourceDirs: Array<String>? = null
    @JvmField var splitPublicSourceDirs: Array<String>? = null
    @JvmField var sharedLibraryFiles: Array<String>? = null
    @JvmField var dataDir: String? = null
    @JvmField var deviceProtectedDataDir: String? = null
    @JvmField var credentialProtectedDataDir: String? = null
    @JvmField var nativeLibraryDir: String? = null
    @JvmField var uid: Int = 0
    @JvmField var minSdkVersion: Int = 21
    @JvmField var targetSdkVersion: Int = 29
    @JvmField var enabled: Boolean = true

    constructor() : super()

    constructor(orig: ApplicationInfo) : super(orig) {
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

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
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

    override fun toString(): String =
        "ApplicationInfo{${hashCode().toString(16)} ${packageName ?: "unknown"}}"

    companion object {
        const val FLAG_SYSTEM: Int = 1 shl 0
        const val FLAG_DEBUGGABLE: Int = 1 shl 1
        const val FLAG_HAS_CODE: Int = 1 shl 2
        const val FLAG_PERSISTENT: Int = 1 shl 3
        const val FLAG_FACTORY_TEST: Int = 1 shl 4
        const val FLAG_ALLOW_TASK_REPARENTING: Int = 1 shl 5
        const val FLAG_ALLOW_CLEAR_USER_DATA: Int = 1 shl 6
        const val FLAG_UPDATED_SYSTEM_APP: Int = 1 shl 7
        const val FLAG_TEST_ONLY: Int = 1 shl 8
        const val FLAG_SUPPORTS_SMALL_SCREENS: Int = 1 shl 9
        const val FLAG_SUPPORTS_NORMAL_SCREENS: Int = 1 shl 10
        const val FLAG_SUPPORTS_LARGE_SCREENS: Int = 1 shl 11
        const val FLAG_RESIZEABLE_FOR_SCREENS: Int = 1 shl 12
        const val FLAG_SUPPORTS_SCREEN_DENSITIES: Int = 1 shl 13
        const val FLAG_VM_SAFE_MODE: Int = 1 shl 14
        const val FLAG_ALLOW_BACKUP: Int = 1 shl 15
        const val FLAG_KILL_AFTER_RESTORE: Int = 1 shl 16
        const val FLAG_RESTORE_ANY_VERSION: Int = 1 shl 17
        const val FLAG_EXTERNAL_STORAGE: Int = 1 shl 18
        const val FLAG_SUPPORTS_XLARGE_SCREENS: Int = 1 shl 19
        const val FLAG_LARGE_HEAP: Int = 1 shl 20
        const val FLAG_STOPPED: Int = 1 shl 21
        const val FLAG_SUPPORTS_RTL: Int = 1 shl 22
        const val FLAG_INSTALLED: Int = 1 shl 23
        const val FLAG_IS_DATA_ONLY: Int = 1 shl 24
        const val FLAG_EXTRACT_NATIVE_LIBS: Int = 1 shl 28
        const val FLAG_HARDWARE_ACCELERATED: Int = 1 shl 29
        const val FLAG_MULTIARCH: Int = 1 shl 31
    }
}
