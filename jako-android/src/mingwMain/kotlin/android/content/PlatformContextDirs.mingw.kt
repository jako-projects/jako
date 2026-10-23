@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package android.content

import java.io.File
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.getenv
import platform.windows.GetModuleFileNameW
import platform.windows.MAX_PATH
import platform.windows.WCHARVar

internal actual fun platformDefaultPackageName(): String {
    val envPkg = getenv("JAKO_PACKAGE_NAME")?.toKString()?.trim()
    if (!envPkg.isNullOrEmpty()) return sanitizeSegment(envPkg)

    val exeName = resolveMingwExecutablePath()
    if (exeName.isNotEmpty()) {
        val baseName = exeName.substringAfterLast('\\').substringAfterLast('/')
            .removeSuffix(".exe")
            .removeSuffix(".kexe")
            .trim()
        if (baseName.isNotEmpty() && baseName != "test") {
            return sanitizeSegment(baseName)
        }
    }
    return "jako.android.app"
}

internal actual fun platformAppDataDir(packageName: String): File {
    val envData = getenv("JAKO_DATA_DIR")?.toKString()?.trim()
    if (!envData.isNullOrEmpty()) return File(envData)

    val pkg = sanitizeSegment(packageName)
    val localAppData = getenv("LOCALAPPDATA")?.toKString()?.trim()
    if (!localAppData.isNullOrEmpty()) {
        return File(localAppData, pkg)
    }
    val appData = getenv("APPDATA")?.toKString()?.trim()
    if (!appData.isNullOrEmpty()) {
        return File(appData, pkg)
    }
    val userProfile = getenv("USERPROFILE")?.toKString()?.trim()
    if (!userProfile.isNullOrEmpty()) {
        return File(File(userProfile, "AppData\\Local"), pkg)
    }
    return File(".", pkg)
}

internal actual fun platformAppCacheDir(packageName: String): File {
    val envCache = getenv("JAKO_CACHE_DIR")?.toKString()?.trim()
    if (!envCache.isNullOrEmpty()) return File(envCache)

    return File(platformAppDataDir(packageName), "cache")
}

internal actual fun platformDefaultAssetsDir(): File {
    val envAssets = getenv("JAKO_ASSETS_DIR")?.toKString()?.trim()
    if (!envAssets.isNullOrEmpty()) return File(envAssets)

    val exePath = resolveMingwExecutablePath()
    if (exePath.isNotEmpty()) {
        val exeDir = File(exePath).parentFile
        if (exeDir != null) {
            val siblingAssets = File(exeDir, "assets")
            if (siblingAssets.isDirectory) return siblingAssets
        }
    }

    return File("assets")
}

internal actual fun platformDefaultResourcesDir(): File {
    val envRes = getenv("JAKO_RESOURCES_DIR")?.toKString()?.trim()
    if (!envRes.isNullOrEmpty()) return File(envRes)

    val exePath = resolveMingwExecutablePath()
    if (exePath.isNotEmpty()) {
        val exeDir = File(exePath).parentFile
        if (exeDir != null) {
            val siblingRes = File(exeDir, "res")
            if (siblingRes.isDirectory) return siblingRes
        }
    }

    return File("res")
}

internal actual fun platformDefaultExternalStorageDir(packageName: String): File {
    val envExt = getenv("JAKO_EXTERNAL_STORAGE_DIR")?.toKString()?.trim()
    if (!envExt.isNullOrEmpty()) return File(envExt)

    return File(platformAppDataDir(packageName), "external_storage")
}

private fun resolveMingwExecutablePath(): String {
    return memScoped {
        val buf = allocArray<WCHARVar>(MAX_PATH)
        val len = GetModuleFileNameW(null, buf, MAX_PATH.toUInt()).toInt()
        if (len > 0) buf.toKString() else ""
    }
}

private fun sanitizeSegment(raw: String): String {
    val cleaned = raw.trim().replace('/', '_').replace('\\', '_').replace(':', '_')
    return if (cleaned.isEmpty() || cleaned == "." || cleaned == "..") "jako.android.app" else cleaned
}
