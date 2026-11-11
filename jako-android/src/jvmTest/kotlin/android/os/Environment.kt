package android.os

import android.content.Context
import com.bennyhuo.jako.JakoApplication
import java.io.File

open class Environment() {
    companion object {
        @JvmField var DIRECTORY_MUSIC: String = "Music"
        @JvmField var DIRECTORY_PODCASTS: String = "Podcasts"
        @JvmField var DIRECTORY_RINGTONES: String = "Ringtones"
        @JvmField var DIRECTORY_ALARMS: String = "Alarms"
        @JvmField var DIRECTORY_NOTIFICATIONS: String = "Notifications"
        @JvmField var DIRECTORY_PICTURES: String = "Pictures"
        @JvmField var DIRECTORY_MOVIES: String = "Movies"
        @JvmField var DIRECTORY_DOWNLOADS: String = "Downloads"
        @JvmField var DIRECTORY_DCIM: String = "DCIM"
        @JvmField var DIRECTORY_DOCUMENTS: String = "Documents"
        @JvmField var DIRECTORY_SCREENSHOTS: String = "Screenshots"
        @JvmField var DIRECTORY_AUDIOBOOKS: String = "Audiobooks"

        const val MEDIA_UNKNOWN: String = "unknown"
        const val MEDIA_REMOVED: String = "removed"
        const val MEDIA_UNMOUNTED: String = "unmounted"
        const val MEDIA_CHECKING: String = "checking"
        const val MEDIA_NOFS: String = "nofs"
        const val MEDIA_MOUNTED: String = "mounted"
        const val MEDIA_MOUNTED_READ_ONLY: String = "mounted_ro"
        const val MEDIA_SHARED: String = "shared"
        const val MEDIA_BAD_REMOVAL: String = "bad_removal"
        const val MEDIA_UNMOUNTABLE: String = "unmountable"
        const val MEDIA_EJECTING: String = "ejecting"

        private fun ensureDir(dir: File): File {
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

        @JvmStatic
        fun getRootDirectory(): File = File("/")

        @JvmStatic
        fun getDataDirectory(): File {
            val cfg = JakoApplication.config
            return ensureDir(cfg.dataDir ?: Context.resolveJvmPlatformAppDataDir(cfg.packageName))
        }

        @JvmStatic
        fun getExternalStorageDirectory(): File {
            val cfg = JakoApplication.config
            return ensureDir(cfg.externalStorageDir ?: Context.resolveJvmDefaultExternalStorageDir(cfg.packageName))
        }

        @JvmStatic
        fun getExternalStoragePublicDirectory(type: String?): File {
            val sub = requireNotNull(type) { "Directory type cannot be null" }.trimStart('/', '\\')
            return ensureDir(File(getExternalStorageDirectory(), sub))
        }

        @JvmStatic
        fun getDownloadCacheDirectory(): File {
            val cfg = JakoApplication.config
            return ensureDir(cfg.cacheDir ?: Context.resolveJvmPlatformAppCacheDir(cfg.packageName))
        }

        @JvmStatic
        fun getExternalStorageState(): String = MEDIA_MOUNTED

        @JvmStatic
        fun getExternalStorageState(path: File?): String =
            if (path != null) MEDIA_MOUNTED else MEDIA_UNKNOWN

        @JvmStatic
        fun isExternalStorageRemovable(): Boolean = false

        @JvmStatic
        fun isExternalStorageRemovable(path: File): Boolean = false

        @JvmStatic
        fun isExternalStorageEmulated(): Boolean = true

        @JvmStatic
        fun isExternalStorageEmulated(path: File): Boolean = true
    }
}
