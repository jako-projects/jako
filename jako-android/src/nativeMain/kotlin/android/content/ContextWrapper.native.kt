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
 * [Nullability Decision] ContextWrapper wraps a base Context (which may be attached via constructor
 * or attachBaseContext) and delegates all Context operations to it, matching AOSP android.content.ContextWrapper.
 */
actual open class ContextWrapper actual constructor(base: Context?) : Context() {
    private var mBase: Context? = base

    actual val baseContext: Context? get() = mBase

    protected open fun attachBaseContext(base: Context?) {
        if (mBase != null) {
            throw IllegalStateException("Base context already set")
        }
        mBase = base
    }

    actual open fun getBaseContext(): Context? = mBase

    override fun getAssets(): AssetManager = mBase?.getAssets() ?: super.getAssets()
    override fun getResources(): Resources = mBase?.getResources() ?: super.getResources()
    override fun getPackageManager(): PackageManager = mBase?.getPackageManager() ?: super.getPackageManager()
    override fun getMainLooper(): Looper? = mBase?.getMainLooper() ?: super.getMainLooper()
    override fun getApplicationInfo(): ApplicationInfo = mBase?.getApplicationInfo() ?: super.getApplicationInfo()
    override fun getDataDir(): File = mBase?.getDataDir() ?: super.getDataDir()
    override fun getFilesDir(): File = mBase?.getFilesDir() ?: super.getFilesDir()
    override fun getNoBackupFilesDir(): File = mBase?.getNoBackupFilesDir() ?: super.getNoBackupFilesDir()
    override fun getExternalFilesDir(type: String?): File? = mBase?.getExternalFilesDir(type) ?: super.getExternalFilesDir(type)
    override fun getExternalFilesDirs(type: String?): Array<File> = mBase?.getExternalFilesDirs(type) ?: super.getExternalFilesDirs(type)
    override fun getObbDir(): File = mBase?.getObbDir() ?: super.getObbDir()
    override fun getObbDirs(): Array<File> = mBase?.getObbDirs() ?: super.getObbDirs()
    override fun getCacheDir(): File = mBase?.getCacheDir() ?: super.getCacheDir()
    override fun getCodeCacheDir(): File = mBase?.getCodeCacheDir() ?: super.getCodeCacheDir()
    override fun getExternalCacheDir(): File? = mBase?.getExternalCacheDir() ?: super.getExternalCacheDir()
    override fun getExternalCacheDirs(): Array<File> = mBase?.getExternalCacheDirs() ?: super.getExternalCacheDirs()
    override fun getDir(name: String?, mode: Int): File = mBase?.getDir(name, mode) ?: super.getDir(name, mode)
    override fun getFileStreamPath(name: String?): File = mBase?.getFileStreamPath(name) ?: super.getFileStreamPath(name)
    override fun openFileInput(name: String?): FileInputStream = mBase?.openFileInput(name) ?: super.openFileInput(name)
    override fun openFileOutput(name: String?, mode: Int): FileOutputStream = mBase?.openFileOutput(name, mode) ?: super.openFileOutput(name, mode)
    override fun deleteFile(name: String?): Boolean = mBase?.deleteFile(name) ?: super.deleteFile(name)
    override fun fileList(): Array<String> = mBase?.fileList() ?: super.fileList()
    override fun getDatabasePath(name: String?): File = mBase?.getDatabasePath(name) ?: super.getDatabasePath(name)
    override fun databaseList(): Array<String> = mBase?.databaseList() ?: super.databaseList()
    override fun deleteDatabase(name: String?): Boolean = mBase?.deleteDatabase(name) ?: super.deleteDatabase(name)
    override fun getPackageName(): String = mBase?.getPackageName() ?: super.getPackageName()
    override fun getApplicationContext(): Context = mBase?.getApplicationContext() ?: super.getApplicationContext()
    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences =
        mBase?.getSharedPreferences(name, mode) ?: super.getSharedPreferences(name, mode)
    override fun deleteSharedPreferences(name: String?): Boolean =
        mBase?.deleteSharedPreferences(name) ?: super.deleteSharedPreferences(name)
    override fun moveSharedPreferencesFrom(sourceContext: Context?, name: String?): Boolean =
        mBase?.moveSharedPreferencesFrom(sourceContext, name) ?: super.moveSharedPreferencesFrom(sourceContext, name)
    override fun checkPermission(permission: String, pid: Int, uid: Int): Int =
        mBase?.checkPermission(permission, pid, uid) ?: super.checkPermission(permission, pid, uid)
    override fun checkSelfPermission(permission: String): Int =
        mBase?.checkSelfPermission(permission) ?: super.checkSelfPermission(permission)
}
