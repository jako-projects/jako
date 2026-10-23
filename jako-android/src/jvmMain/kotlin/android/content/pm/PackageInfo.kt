package android.content.pm

import android.os.Parcel
import android.os.Parcelable

class PackageInfo() : Parcelable {
    @JvmField var packageName: String? = null
    @JvmField var splitNames: Array<String>? = null
    @JvmField var versionCode: Int = 0
    private var versionCodeMajor: Int = 0
    @JvmField var versionName: String? = null
    @JvmField var sharedUserId: String? = null
    @JvmField var sharedUserLabel: Int = 0
    @JvmField var applicationInfo: ApplicationInfo? = null
    @JvmField var firstInstallTime: Long = 0L
    @JvmField var lastUpdateTime: Long = 0L

    fun getLongVersionCode(): Long {
        return (versionCodeMajor.toLong() shl 32) or (versionCode.toLong() and 0xFFFFFFFFL)
    }

    fun setLongVersionCode(versionCode: Long) {
        this.versionCode = versionCode.toInt()
        this.versionCodeMajor = (versionCode ushr 32).toInt()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(packageName)
        dest.writeInt(versionCode)
        dest.writeInt(versionCodeMajor)
        dest.writeString(versionName)
        dest.writeLong(firstInstallTime)
        dest.writeLong(lastUpdateTime)
    }

    override fun toString(): String =
        "PackageInfo{${hashCode().toString(16)} ${packageName ?: "unknown"}}"
}
