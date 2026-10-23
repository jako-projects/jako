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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Desktop JVM implementation of `android.content.Context` using the same OS-standard persistent
 * application directories and multi-level resource discovery as Native (`~/Library/Application Support`
 * on macOS, `$XDG_DATA_HOME` / `~/.local/share` on Linux, `%LOCALAPPDATA%` on Windows).
 */
abstract class Context {
    private var cachedAssets: AssetManager? = null
    private var cachedResources: Resources? = null
    private var cachedPackageManager: PackageManager? = null

    open fun getAssets(): AssetManager {
        var a = cachedAssets
        if (a == null) {
            a = AssetManager()
            cachedAssets = a
        }
        return a
    }

    open fun getResources(): Resources {
        var r = cachedResources
        if (r == null) {
            r = Resources(getAssets(), DisplayMetrics(), Configuration())
            cachedResources = r
        }
        return r
    }

    open fun getPackageManager(): PackageManager {
        var pm = cachedPackageManager
        if (pm == null) {
            pm = JvmDefaultPackageManager(this)
            cachedPackageManager = pm
        }
        return pm
    }

    open fun getMainLooper(): Looper? {
        if (Looper.getMainLooper() == null && Looper.myLooper() == null) {
            try {
                Looper.prepareMainLooper()
            } catch (_: Throwable) {
            }
        }
        return Looper.getMainLooper() ?: Looper.myLooper()
    }

    open fun getPackageName(): String = JakoApplication.config.packageName

    open fun getApplicationContext(): Context = this

    open fun getApplicationInfo(): ApplicationInfo {
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
            sourceDir = (cfg.assetsDir ?: resolveJvmDefaultAssetsDir()).absolutePath
            publicSourceDir = sourceDir
            nativeLibraryDir = (cfg.nativeLibraryDir ?: File(getDataDir(), "lib")).absolutePath
            minSdkVersion = cfg.minSdkVersion
            targetSdkVersion = cfg.targetSdkVersion
            flags = ApplicationInfo.FLAG_HAS_CODE or ApplicationInfo.FLAG_ALLOW_BACKUP or ApplicationInfo.FLAG_INSTALLED
            metaData = Bundle(cfg.metaDataBundle)
        }
    }

    open fun getDataDir(): File {
        val customData = JakoApplication.config.dataDir
        val pkg = getPackageName()
        val dir = if (customData != null && pkg == JakoApplication.config.packageName) {
            customData
        } else {
            resolveJvmPlatformAppDataDir(pkg)
        }
        return ensureDirExists(dir)
    }

    open fun getFilesDir(): File {
        val customFiles = JakoApplication.config.filesDir
        if (customFiles != null && getPackageName() == JakoApplication.config.packageName) {
            return ensureDirExists(customFiles)
        }
        return ensureDirExists(File(getDataDir(), "files"))
    }

    open fun getNoBackupFilesDir(): File = ensureDirExists(File(getDataDir(), "no_backup"))

    open fun getCacheDir(): File {
        val customCache = JakoApplication.config.cacheDir
        val pkg = getPackageName()
        val dir = if (customCache != null && pkg == JakoApplication.config.packageName) {
            customCache
        } else {
            resolveJvmPlatformAppCacheDir(pkg)
        }
        return ensureDirExists(dir)
    }

    open fun getCodeCacheDir(): File = ensureDirExists(File(getCacheDir(), "code_cache"))

    private fun resolveExternalAppBaseDir(): File {
        val pkg = getPackageName()
        val extRoot = JakoApplication.config.externalStorageDir ?: resolveJvmDefaultExternalStorageDir(pkg)
        return File(File(extRoot, "Android/data"), pkg)
    }

    open fun getExternalFilesDir(type: String?): File? {
        val base = File(resolveExternalAppBaseDir(), "files")
        val target = if (type.isNullOrEmpty()) base else File(base, type.trimStart('/', '\\'))
        return ensureDirExists(target)
    }

    open fun getExternalFilesDirs(type: String?): Array<File> {
        val dir = getExternalFilesDir(type)
        return if (dir != null) arrayOf(dir) else emptyArray()
    }

    open fun getExternalCacheDir(): File? {
        return ensureDirExists(File(resolveExternalAppBaseDir(), "cache"))
    }

    open fun getExternalCacheDirs(): Array<File> {
        val dir = getExternalCacheDir()
        return if (dir != null) arrayOf(dir) else emptyArray()
    }

    open fun getObbDir(): File {
        val pkg = getPackageName()
        val extRoot = JakoApplication.config.externalStorageDir ?: resolveJvmDefaultExternalStorageDir(pkg)
        return ensureDirExists(File(File(extRoot, "Android/obb"), pkg))
    }

    open fun getObbDirs(): Array<File> = arrayOf(getObbDir())

    open fun getDir(name: String?, mode: Int): File {
        val dirName = "app_${name ?: ""}"
        return ensureDirExists(makeFilename(getDataDir(), dirName))
    }

    open fun getFileStreamPath(name: String?): File {
        return makeFilename(getFilesDir(), requireValidFileName(name))
    }

    open fun openFileInput(name: String?): FileInputStream {
        return FileInputStream(getFileStreamPath(name))
    }

    open fun openFileOutput(name: String?, mode: Int): FileOutputStream {
        val append = (mode and MODE_APPEND) != 0
        val f = getFileStreamPath(name)
        val parent = f.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
        return FileOutputStream(f, append)
    }

    open fun deleteFile(name: String?): Boolean {
        return getFileStreamPath(name).delete()
    }

    open fun fileList(): Array<String> {
        return getFilesDir().list() ?: emptyArray()
    }

    open fun getDatabasePath(name: String?): File {
        val dbName = requireNotNull(name) { "Database name cannot be null" }
        return if (dbName.indexOf(File.separatorChar) >= 0) {
            File(dbName)
        } else {
            val dbDir = ensureDirExists(File(getDataDir(), "databases"))
            makeFilename(dbDir, dbName)
        }
    }

    open fun databaseList(): Array<String> {
        val dbDir = File(getDataDir(), "databases")
        return dbDir.list() ?: emptyArray()
    }

    open fun deleteDatabase(name: String?): Boolean {
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

    open fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
        return JvmSharedPreferencesRegistry.getOrCreate(getSharedPreferencesPath(name))
    }

    open fun deleteSharedPreferences(name: String?): Boolean {
        return JvmSharedPreferencesRegistry.delete(getSharedPreferencesPath(name))
    }

    open fun moveSharedPreferencesFrom(sourceContext: Context?, name: String?): Boolean {
        val src = requireNotNull(sourceContext) { "sourceContext cannot be null" }
        return JvmSharedPreferencesRegistry.move(
            src.getSharedPreferencesPath(name),
            this.getSharedPreferencesPath(name)
        )
    }

    fun getText(resId: Int): CharSequence = getResources().getText(resId)

    fun getString(resId: Int): String = getResources().getString(resId)

    fun getString(resId: Int, vararg formatArgs: Any?): String =
        getResources().getString(resId, *formatArgs)

    fun getColor(id: Int): Int = getResources().getColor(id)

    open fun checkPermission(permission: String, pid: Int, uid: Int): Int =
        PackageManager.PERMISSION_GRANTED

    open fun checkSelfPermission(permission: String): Int =
        PackageManager.PERMISSION_GRANTED

    private fun ensureDirExists(dir: File): File {
        if (!dir.exists()) dir.mkdirs()
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

    companion object {
        const val MODE_PRIVATE: Int = 0x0000
        const val MODE_APPEND: Int = 0x8000

        internal fun sanitizeSegment(raw: String): String {
            val cleaned = raw.trim().replace('/', '_').replace('\\', '_').replace(':', '_')
            return if (cleaned.isEmpty() || cleaned == "." || cleaned == "..") "jako.android.app" else cleaned
        }

        internal fun resolveJvmDefaultPackageName(): String {
            val envPkg = System.getenv("JAKO_PACKAGE_NAME")?.trim()
            return if (!envPkg.isNullOrEmpty()) sanitizeSegment(envPkg) else "jako.android.app"
        }

        internal fun resolveJvmPlatformAppDataDir(packageName: String): File {
            val envData = System.getenv("JAKO_DATA_DIR")?.trim()
            if (!envData.isNullOrEmpty()) return File(envData)

            val pkg = sanitizeSegment(packageName)
            val os = System.getProperty("os.name", "").lowercase()
            val home = System.getProperty("user.home", ".")
            return when {
                "mac" in os || "darwin" in os -> File(File(home, "Library/Application Support"), pkg)
                "win" in os -> {
                    val localAppData = System.getenv("LOCALAPPDATA")?.takeIf { it.isNotEmpty() }
                        ?: System.getenv("APPDATA")?.takeIf { it.isNotEmpty() }
                        ?: File(home, "AppData\\Local").path
                    File(localAppData, pkg)
                }
                else -> {
                    val xdgData = System.getenv("XDG_DATA_HOME")?.takeIf { it.startsWith("/") }
                        ?: File(home, ".local/share").path
                    File(xdgData, pkg)
                }
            }
        }

        internal fun resolveJvmPlatformAppCacheDir(packageName: String): File {
            val envCache = System.getenv("JAKO_CACHE_DIR")?.trim()
            if (!envCache.isNullOrEmpty()) return File(envCache)

            val pkg = sanitizeSegment(packageName)
            val os = System.getProperty("os.name", "").lowercase()
            val home = System.getProperty("user.home", ".")
            return when {
                "mac" in os || "darwin" in os -> File(File(home, "Library/Caches"), pkg)
                "win" in os -> File(resolveJvmPlatformAppDataDir(packageName), "cache")
                else -> {
                    val xdgCache = System.getenv("XDG_CACHE_HOME")?.takeIf { it.startsWith("/") }
                        ?: File(home, ".cache").path
                    File(xdgCache, pkg)
                }
            }
        }

        internal fun resolveJvmDefaultAssetsDir(): File {
            val envAssets = System.getenv("JAKO_ASSETS_DIR")?.trim()
            if (!envAssets.isNullOrEmpty()) return File(envAssets)
            return File("assets")
        }

        internal fun resolveJvmDefaultResourcesDir(): File {
            val envRes = System.getenv("JAKO_RESOURCES_DIR")?.trim()
            if (!envRes.isNullOrEmpty()) return File(envRes)
            return File("res")
        }

        internal fun resolveJvmDefaultExternalStorageDir(packageName: String): File {
            val envExt = System.getenv("JAKO_EXTERNAL_STORAGE_DIR")?.trim()
            if (!envExt.isNullOrEmpty()) return File(envExt)
            return File(resolveJvmPlatformAppDataDir(packageName), "external_storage")
        }
    }
}

private class JvmDefaultPackageManager(private val context: Context) : PackageManager() {
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
