package android.content.pm

import android.os.Parcel
import android.os.Parcelable

actual class PackageInfo actual constructor() : Parcelable {
    actual var packageName: String? = null
    actual var splitNames: Array<String>? = null
    actual var versionCode: Int = 0
    private var versionCodeMajor: Int = 0
    actual var versionName: String? = null
    actual var sharedUserId: String? = null
    actual var sharedUserLabel: Int = 0
    actual var applicationInfo: ApplicationInfo? = null
    actual var firstInstallTime: Long = 0L
    actual var lastUpdateTime: Long = 0L

    actual fun getLongVersionCode(): Long {
        return (versionCodeMajor.toLong() shl 32) or (versionCode.toLong() and 0xFFFFFFFFL)
    }

    actual fun setLongVersionCode(versionCode: Long) {
        this.versionCode = versionCode.toInt()
        this.versionCodeMajor = (versionCode ushr 32).toInt()
    }

    actual override fun describeContents(): Int = 0

    actual override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(packageName)
        dest.writeInt(versionCode)
        dest.writeInt(versionCodeMajor)
        dest.writeString(versionName)
        dest.writeLong(firstInstallTime)
        dest.writeLong(lastUpdateTime)
    }

    actual override fun toString(): String =
        "PackageInfo{${hashCode().toString(16)} ${packageName ?: "unknown"}}"
}
