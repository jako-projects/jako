@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.content

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Looper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * [Nullability Decision] Context directory, file stream, database, assets, resources, and SharedPreferences accessors
 * return non-null instances, matching AOSP android.content.Context. External directory accessors (`getExternalFilesDir`,
 * `getExternalCacheDir`) return `File?` matching AOSP signatures, and always return a valid directory on PC/Native.
 */
expect abstract class Context() {
    open val assets: AssetManager
    open val resources: Resources
    open val packageManager: PackageManager
    open val mainLooper: Looper?
    open val applicationContext: Context
    open val packageName: String
    open val applicationInfo: ApplicationInfo
    open val dataDir: File
    open val filesDir: File
    open val noBackupFilesDir: File
    open val externalCacheDir: File?
    open val externalCacheDirs: Array<File>
    open val obbDir: File
    open val obbDirs: Array<File>
    open val cacheDir: File
    open val codeCacheDir: File

    open fun getAssets(): AssetManager
    open fun getResources(): Resources
    open fun getPackageManager(): PackageManager
    open fun getMainLooper(): Looper?
    open fun getApplicationContext(): Context
    open fun getPackageName(): String
    open fun getApplicationInfo(): ApplicationInfo

    open fun getDataDir(): File
    open fun getFilesDir(): File
    open fun getNoBackupFilesDir(): File
    open fun getExternalFilesDir(type: String?): File?
    open fun getExternalFilesDirs(type: String?): Array<File>
    open fun getObbDir(): File
    open fun getObbDirs(): Array<File>
    open fun getCacheDir(): File
    open fun getCodeCacheDir(): File
    open fun getExternalCacheDir(): File?
    open fun getExternalCacheDirs(): Array<File>
    open fun getDir(name: String?, mode: Int): File
    open fun getFileStreamPath(name: String?): File
    open fun openFileInput(name: String?): FileInputStream
    open fun openFileOutput(name: String?, mode: Int): FileOutputStream
    open fun deleteFile(name: String?): Boolean
    open fun fileList(): Array<String>
    open fun getDatabasePath(name: String?): File
    open fun databaseList(): Array<String>
    open fun deleteDatabase(name: String?): Boolean
    open fun getSharedPreferences(name: String?, mode: Int): SharedPreferences
    open fun deleteSharedPreferences(name: String?): Boolean
    open fun moveSharedPreferencesFrom(sourceContext: Context?, name: String?): Boolean

    fun getText(resId: Int): CharSequence
    fun getString(resId: Int): String
    fun getString(resId: Int, vararg formatArgs: Any?): String
    fun getColor(id: Int): Int
    open fun checkPermission(permission: String, pid: Int, uid: Int): Int
    open fun checkSelfPermission(permission: String): Int

    companion object {
        val MODE_PRIVATE: Int
        val MODE_APPEND: Int
    }
}
