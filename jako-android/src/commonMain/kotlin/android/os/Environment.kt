@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

import java.io.File

/**
 * Provides access to environment variables and standard external storage directories.
 *
 * [Nullability Decision]:
 * - `getExternalStorageDirectory()`, `getDataDirectory()`, `getDownloadCacheDirectory()`, and `getRootDirectory()`
 *   return non-null `File` instances matching AOSP `android.os.Environment`.
 */
expect open class Environment() {
    companion object {
        var DIRECTORY_MUSIC: String
        var DIRECTORY_PODCASTS: String
        var DIRECTORY_RINGTONES: String
        var DIRECTORY_ALARMS: String
        var DIRECTORY_NOTIFICATIONS: String
        var DIRECTORY_PICTURES: String
        var DIRECTORY_MOVIES: String
        var DIRECTORY_DOWNLOADS: String
        var DIRECTORY_DCIM: String
        var DIRECTORY_DOCUMENTS: String
        var DIRECTORY_SCREENSHOTS: String
        var DIRECTORY_AUDIOBOOKS: String

        val MEDIA_UNKNOWN: String
        val MEDIA_REMOVED: String
        val MEDIA_UNMOUNTED: String
        val MEDIA_CHECKING: String
        val MEDIA_NOFS: String
        val MEDIA_MOUNTED: String
        val MEDIA_MOUNTED_READ_ONLY: String
        val MEDIA_SHARED: String
        val MEDIA_BAD_REMOVAL: String
        val MEDIA_UNMOUNTABLE: String
        val MEDIA_EJECTING: String

        fun getRootDirectory(): File
        fun getDataDirectory(): File
        fun getExternalStorageDirectory(): File
        fun getExternalStoragePublicDirectory(type: String?): File
        fun getDownloadCacheDirectory(): File
        fun getExternalStorageState(): String
        fun getExternalStorageState(path: File?): String
        fun isExternalStorageRemovable(): Boolean
        fun isExternalStorageRemovable(path: File): Boolean
        fun isExternalStorageEmulated(): Boolean
        fun isExternalStorageEmulated(path: File): Boolean
    }
}
