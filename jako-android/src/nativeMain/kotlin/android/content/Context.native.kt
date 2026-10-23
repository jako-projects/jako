package android.content

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Looper
import android.util.DisplayMetrics
import com.bennyhuo.jako.JakoApplication
import com.bennyhuo.jako.platformDefaultPackageNameProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

internal expect fun platformDefaultPackageName(): String

internal expect fun platformAppDataDir(packageName: String): File

internal expect fun platformAppCacheDir(packageName: String): File

internal expect fun platformDefaultAssetsDir(): File

internal expect fun platformDefaultResourcesDir(): File

internal expect fun platformDefaultExternalStorageDir(packageName: String): File

/**
 * [Nullability Decision] Context directory, file stream, database, assets, resources, and SharedPreferences accessors
 * return non-null instances, matching AOSP android.content.Context.
 */
actual abstract class Context actual constructor() {
    init {
        if (platformDefaultPackageNameProvider == null) {
            platformDefaultPackageNameProvider = ::platformDefaultPackageName
        }
    }

    private var cachedAssets: AssetManager? = null
    private var cachedResources: Resources? = null
    private var cachedPackageManager: PackageManager? = null

    actual open val assets: AssetManager get() = getAssets()
    actual open val resources: Resources get() = getResources()
    actual open val packageManager: PackageManager get() = getPackageManager()
    actual open val mainLooper: Looper? get() = getMainLooper()
    actual open val applicationContext: Context get() = getApplicationContext()
    actual open val packageName: String get() = getPackageName()
    actual open val applicationInfo: ApplicationInfo get() = getApplicationInfo()
    actual open val dataDir: File get() = getDataDir()
    actual open val filesDir: File get() = getFilesDir()
    actual open val noBackupFilesDir: File get() = getNoBackupFilesDir()
    actual open val externalCacheDir: File? get() = getExternalCacheDir()
    actual open val externalCacheDirs: Array<File> get() = getExternalCacheDirs()
    actual open val obbDir: File get() = getObbDir()
    actual open val obbDirs: Array<File> get() = getObbDirs()
    actual open val cacheDir: File get() = getCacheDir()
    actual open val codeCacheDir: File get() = getCodeCacheDir()

    actual open fun getAssets(): AssetManager {
        var a = cachedAssets
        if (a == null) {
            a = AssetManager()
            cachedAssets = a
        }
        return a
    }

    actual open fun getResources(): Resources {
        var r = cachedResources
        if (r == null) {
            r = Resources(getAssets(), DisplayMetrics(), Configuration())
            cachedResources = r
        }
        return r
    }

    actual open fun getPackageManager(): PackageManager {
        var pm = cachedPackageManager
        if (pm == null) {
            pm = DefaultPackageManager(this)
            cachedPackageManager = pm
        }
        return pm
    }

    actual open fun getMainLooper(): Looper? {
        if (Looper.getMainLooper() == null && Looper.myLooper() == null) {
            try {
                Looper.prepareMainLooper()
            } catch (_: Throwable) {
            }
        }
        return Looper.getMainLooper() ?: Looper.myLooper()
    }

    actual open fun getPackageName(): String =
        JakoApplication.config.explicitPackageName ?: platformDefaultPackageName()

    actual open fun getApplicationContext(): Context = this

    actual open fun getApplicationInfo(): ApplicationInfo {
        val cfg = JakoApplication.config
        val pkg = getPackageName()
        val dataPath = getDataDir().absolutePath
        return ApplicationInfo().apply {
            name = pkg
            packageName = pkg
            processName = pkg
            dataDir = dataPath
            deviceProtectedDataDir = dataPath
            credentialProtectedDataDir = dataPath
            sourceDir = (cfg.assetsDir ?: platformDefaultAssetsDir()).absolutePath
            publicSourceDir = sourceDir
            nativeLibraryDir = (cfg.nativeLibraryDir ?: File(getDataDir(), "lib")).absolutePath
            minSdkVersion = cfg.minSdkVersion
            targetSdkVersion = cfg.targetSdkVersion
            flags = ApplicationInfo.FLAG_HAS_CODE or ApplicationInfo.FLAG_ALLOW_BACKUP or ApplicationInfo.FLAG_INSTALLED
            metaData = Bundle(cfg.metaDataBundle)
        }
    }

    actual open fun getDataDir(): File {
        val customData = JakoApplication.config.dataDir
        val pkg = getPackageName()
        val dir = if (customData != null && pkg == JakoApplication.config.packageName) {
            customData
        } else {
            platformAppDataDir(pkg)
        }
        return ensureDirExists(dir)
    }

    actual open fun getFilesDir(): File {
        val customFiles = JakoApplication.config.filesDir
        if (customFiles != null && getPackageName() == JakoApplication.config.packageName) {
            return ensureDirExists(customFiles)
        }
        return ensureDirExists(File(getDataDir(), "files"))
    }

    actual open fun getNoBackupFilesDir(): File = ensureDirExists(File(getDataDir(), "no_backup"))

    actual open fun getCacheDir(): File {
        val customCache = JakoApplication.config.cacheDir
        val pkg = getPackageName()
        val dir = if (customCache != null && pkg == JakoApplication.config.packageName) {
            customCache
        } else {
            platformAppCacheDir(pkg)
        }
        return ensureDirExists(dir)
    }

    actual open fun getCodeCacheDir(): File = ensureDirExists(File(getCacheDir(), "code_cache"))

    private fun resolveExternalAppBaseDir(): File {
        val pkg = getPackageName()
        val extRoot = JakoApplication.config.externalStorageDir ?: platformDefaultExternalStorageDir(pkg)
        return File(File(extRoot, "Android/data"), pkg)
    }

    actual open fun getExternalFilesDir(type: String?): File? {
        val base = File(resolveExternalAppBaseDir(), "files")
        val target = if (type.isNullOrEmpty()) base else File(base, type.trimStart('/', '\\'))
        return ensureDirExists(target)
    }

    actual open fun getExternalFilesDirs(type: String?): Array<File> {
        val dir = getExternalFilesDir(type)
        return if (dir != null) arrayOf(dir) else emptyArray()
    }

    actual open fun getExternalCacheDir(): File? {
        return ensureDirExists(File(resolveExternalAppBaseDir(), "cache"))
    }

    actual open fun getExternalCacheDirs(): Array<File> {
        val dir = getExternalCacheDir()
        return if (dir != null) arrayOf(dir) else emptyArray()
    }

    actual open fun getObbDir(): File {
        val pkg = getPackageName()
        val extRoot = JakoApplication.config.externalStorageDir ?: platformDefaultExternalStorageDir(pkg)
        return ensureDirExists(File(File(extRoot, "Android/obb"), pkg))
    }

    actual open fun getObbDirs(): Array<File> = arrayOf(getObbDir())

    actual open fun getDir(name: String?, mode: Int): File {
        val dirName = "app_${name ?: ""}"
        return ensureDirExists(makeFilename(getDataDir(), dirName))
    }

    actual open fun getFileStreamPath(name: String?): File {
        return makeFilename(getFilesDir(), requireValidFileName(name))
    }

    actual open fun openFileInput(name: String?): FileInputStream {
        val f = getFileStreamPath(name)
        return FileInputStream(f)
    }

    actual open fun openFileOutput(name: String?, mode: Int): FileOutputStream {
        val append = (mode and MODE_APPEND) != 0
        val f = getFileStreamPath(name)
        val parent = f.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
        return FileOutputStream(f, append)
    }

    actual open fun deleteFile(name: String?): Boolean {
        val f = getFileStreamPath(name)
        return f.delete()
    }

    actual open fun fileList(): Array<String> {
        return getFilesDir().list() ?: emptyArray()
    }

    actual open fun getDatabasePath(name: String?): File {
        val dbName = requireNotNull(name) { "Database name cannot be null" }
        return if (dbName.indexOf(File.separatorChar) >= 0) {
            File(dbName)
        } else {
            val dbDir = ensureDirExists(File(getDataDir(), "databases"))
            makeFilename(dbDir, dbName)
        }
    }

    actual open fun databaseList(): Array<String> {
        val dbDir = File(getDataDir(), "databases")
        return dbDir.list() ?: emptyArray()
    }

    actual open fun deleteDatabase(name: String?): Boolean {
        return try {
            val f = getDatabasePath(name)
            File("${f.path}-journal").delete()
            File("${f.path}-wal").delete()
            File("${f.path}-shm").delete()
            f.delete()
        } catch (_: Throwable) {
            false
        }
    }

    internal fun getSharedPreferencesPath(name: String?): File {
        val safeName = if (name.isNullOrEmpty()) "null" else requireValidFileName(name)
        val prefsDir = ensureDirExists(File(getDataDir(), "shared_prefs"))
        return makeFilename(prefsDir, "$safeName.xml")
    }

    actual open fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
        val file = getSharedPreferencesPath(name)
        return SharedPreferencesRegistry.getOrCreate(file)
    }

    actual open fun deleteSharedPreferences(name: String?): Boolean {
        val file = getSharedPreferencesPath(name)
        return SharedPreferencesRegistry.delete(file)
    }

    actual open fun moveSharedPreferencesFrom(sourceContext: Context?, name: String?): Boolean {
        val src = requireNotNull(sourceContext) { "sourceContext cannot be null" }
        val srcFile = src.getSharedPreferencesPath(name)
        val dstFile = this.getSharedPreferencesPath(name)
        return SharedPreferencesRegistry.move(srcFile, dstFile)
    }

    actual fun getText(resId: Int): CharSequence = getResources().getText(resId)

    actual fun getString(resId: Int): String = getResources().getString(resId)

    actual fun getString(resId: Int, vararg formatArgs: Any?): String =
        getResources().getString(resId, *formatArgs)

    actual fun getColor(id: Int): Int = getResources().getColor(id)

    actual open fun checkPermission(permission: String, pid: Int, uid: Int): Int =
        PackageManager.PERMISSION_GRANTED

    actual open fun checkSelfPermission(permission: String): Int =
        PackageManager.PERMISSION_GRANTED

    private fun ensureDirExists(dir: File): File {
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun requireValidFileName(name: String?): String {
        val n = requireNotNull(name) { "File name cannot be null" }
        if (n.indexOf('/') >= 0 || n.indexOf('\\') >= 0 || n.indexOf(File.separatorChar) >= 0) {
            throw IllegalArgumentException("File $n contains a path separator")
        }
        return n
    }

    private fun makeFilename(base: File, name: String): File {
        if (name.indexOf('/') >= 0 || name.indexOf('\\') >= 0 || name.indexOf(File.separatorChar) >= 0) {
            throw IllegalArgumentException("File $name contains a path separator")
        }
        return File(base, name)
    }

    actual companion object {
        actual const val MODE_PRIVATE: Int = 0x0000
        actual const val MODE_APPEND: Int = 0x8000
    }
}

internal class DefaultPackageManager(private val context: Context) : PackageManager() {
    override fun getPackageInfo(packageName: String, flags: Int): PackageInfo {
        if (packageName != context.getPackageName() && packageName != JakoApplication.config.packageName) {
            throw NameNotFoundException("Package not found: $packageName")
        }
        val cfg = JakoApplication.config
        return PackageInfo().apply {
            this.packageName = packageName
            this.versionName = cfg.versionName
            this.versionCode = cfg.versionCode
            if (cfg.longVersionCode != 1L || cfg.versionCode == 1) {
                setLongVersionCode(if (cfg.longVersionCode != 1L) cfg.longVersionCode else cfg.versionCode.toLong())
            }
            this.applicationInfo = context.getApplicationInfo()
        }
    }

    override fun getApplicationInfo(packageName: String, flags: Int): ApplicationInfo {
        if (packageName != context.getPackageName() && packageName != JakoApplication.config.packageName) {
            throw NameNotFoundException("Application not found: $packageName")
        }
        return context.getApplicationInfo()
    }

    override fun getApplicationLabel(info: ApplicationInfo): CharSequence {
        return info.nonLocalizedLabel ?: info.name ?: info.packageName ?: ""
    }

    override fun checkPermission(permName: String, pkgName: String): Int = PERMISSION_GRANTED

    override fun hasSystemFeature(name: String): Boolean = false

    override fun hasSystemFeature(name: String, version: Int): Boolean = false
}
