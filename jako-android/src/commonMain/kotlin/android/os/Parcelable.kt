@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

/**
 * Interface for classes whose instances can be written to and restored from a [Parcel].
 */
expect interface Parcelable {
    fun describeContents(): Int
    fun writeToParcel(dest: Parcel, flags: Int)

    interface Creator<T> {
        fun createFromParcel(source: Parcel): T
        fun newArray(size: Int): Array<T?>
    }

    companion object {
        val PARCELABLE_WRITE_RETURN_VALUE: Int
        val CONTENTS_FILE_DESCRIPTOR: Int
    }
}
