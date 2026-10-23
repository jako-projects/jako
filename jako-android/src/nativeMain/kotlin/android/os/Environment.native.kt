package android.os

import android.content.platformAppCacheDir
import android.content.platformAppDataDir
import android.content.platformDefaultExternalStorageDir
import com.bennyhuo.jako.JakoApplication
import java.io.File
import kotlinx.cinterop.sizeOf
import platform.posix.getpid

actual open class Environment actual constructor() {
    actual companion object {
        actual var DIRECTORY_MUSIC: String = "Music"
        actual var DIRECTORY_PODCASTS: String = "Podcasts"
        actual var DIRECTORY_RINGTONES: String = "Ringtones"
        actual var DIRECTORY_ALARMS: String = "Alarms"
        actual var DIRECTORY_NOTIFICATIONS: String = "Notifications"
        actual var DIRECTORY_PICTURES: String = "Pictures"
        actual var DIRECTORY_MOVIES: String = "Movies"
        actual var DIRECTORY_DOWNLOADS: String = "Downloads"
        actual var DIRECTORY_DCIM: String = "DCIM"
        actual var DIRECTORY_DOCUMENTS: String = "Documents"
        actual var DIRECTORY_SCREENSHOTS: String = "Screenshots"
        actual var DIRECTORY_AUDIOBOOKS: String = "Audiobooks"

        actual const val MEDIA_UNKNOWN: String = "unknown"
        actual const val MEDIA_REMOVED: String = "removed"
        actual const val MEDIA_UNMOUNTED: String = "unmounted"
        actual const val MEDIA_CHECKING: String = "checking"
        actual const val MEDIA_NOFS: String = "nofs"
        actual const val MEDIA_MOUNTED: String = "mounted"
        actual const val MEDIA_MOUNTED_READ_ONLY: String = "mounted_ro"
        actual const val MEDIA_SHARED: String = "shared"
        actual const val MEDIA_BAD_REMOVAL: String = "bad_removal"
        actual const val MEDIA_UNMOUNTABLE: String = "unmountable"
        actual const val MEDIA_EJECTING: String = "ejecting"

        private fun ensureDir(dir: File): File {
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

        actual fun getRootDirectory(): File = File("/")

        actual fun getDataDirectory(): File {
            val cfg = JakoApplication.config
            return ensureDir(cfg.dataDir ?: platformAppDataDir(cfg.packageName))
        }

        actual fun getExternalStorageDirectory(): File {
            val cfg = JakoApplication.config
            return ensureDir(cfg.externalStorageDir ?: platformDefaultExternalStorageDir(cfg.packageName))
        }

        actual fun getExternalStoragePublicDirectory(type: String?): File {
            val sub = requireNotNull(type) { "Directory type cannot be null" }.trimStart('/', '\\')
            return ensureDir(File(getExternalStorageDirectory(), sub))
        }

        actual fun getDownloadCacheDirectory(): File {
            val cfg = JakoApplication.config
            return ensureDir(cfg.cacheDir ?: platformAppCacheDir(cfg.packageName))
        }

        actual fun getExternalStorageState(): String = MEDIA_MOUNTED

        actual fun getExternalStorageState(path: File?): String =
            if (path != null) MEDIA_MOUNTED else MEDIA_UNKNOWN

        actual fun isExternalStorageRemovable(): Boolean = false

        actual fun isExternalStorageRemovable(path: File): Boolean = false

        actual fun isExternalStorageEmulated(): Boolean = true

        actual fun isExternalStorageEmulated(path: File): Boolean = true
    }
}
