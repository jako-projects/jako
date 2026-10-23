@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.content.pm

import android.os.Parcel
import android.os.Parcelable

/**
 * Overall information about the contents of a package collected from the manifest.
 */
expect class PackageInfo() : Parcelable {
    var packageName: String?
    var splitNames: Array<String>?
    var versionCode: Int
    var versionName: String?
    var sharedUserId: String?
    var sharedUserLabel: Int
    var applicationInfo: ApplicationInfo?
    var firstInstallTime: Long
    var lastUpdateTime: Long

    fun getLongVersionCode(): Long
    fun setLongVersionCode(versionCode: Long)
    override fun describeContents(): Int
    override fun writeToParcel(dest: Parcel, flags: Int)
    override fun toString(): String
}
