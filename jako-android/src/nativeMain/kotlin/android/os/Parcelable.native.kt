package android.os

import android.content.platformAppCacheDir
import android.content.platformAppDataDir
import android.content.platformDefaultExternalStorageDir
import kotlinx.cinterop.sizeOf
import platform.posix.getpid

actual interface Parcelable {
    actual fun describeContents(): Int
    actual fun writeToParcel(dest: Parcel, flags: Int)

    actual interface Creator<T> {
        actual fun createFromParcel(source: Parcel): T
        actual fun newArray(size: Int): Array<T?>
    }

    actual companion object {
        actual val PARCELABLE_WRITE_RETURN_VALUE: Int = 1
        actual val CONTENTS_FILE_DESCRIPTOR: Int = 1
    }
}
