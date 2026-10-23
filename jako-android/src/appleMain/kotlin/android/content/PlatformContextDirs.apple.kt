@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package android.content

import java.io.File
import kotlinx.cinterop.toKString
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.posix.getenv

internal actual fun platformDefaultPackageName(): String {
    val envPkg = getenv("JAKO_PACKAGE_NAME")?.toKString()?.trim()
    if (!envPkg.isNullOrEmpty()) return sanitizeSegment(envPkg)

    val bundleId = NSBundle.mainBundle.bundleIdentifier?.trim()
    if (!bundleId.isNullOrEmpty()) return bundleId

    val bundleName = (NSBundle.mainBundle.infoDictionary?.get("CFBundleName") as? String)?.trim()
    if (!bundleName.isNullOrEmpty()) return sanitizeSegment(bundleName)

    val procName = NSProcessInfo.processInfo.processName.trim()
        .removeSuffix(".kexe")
    if (procName.isNotEmpty() && procName != "test") {
        return sanitizeSegment(procName)
    }
    return "jako.android.app"
}

internal actual fun platformAppDataDir(packageName: String): File {
    val envData = getenv("JAKO_DATA_DIR")?.toKString()?.trim()
    if (!envData.isNullOrEmpty()) return File(envData)

    val paths = NSSearchPathForDirectoriesInDomains(
        NSApplicationSupportDirectory,
        NSUserDomainMask,
        true
    )
    val base = (paths.firstOrNull() as? String)?.takeIf { it.isNotEmpty() }
        ?: "${NSHomeDirectory()}/Library/Application Support"
    return File(base, sanitizeSegment(packageName))
}

internal actual fun platformAppCacheDir(packageName: String): File {
    val envCache = getenv("JAKO_CACHE_DIR")?.toKString()?.trim()
    if (!envCache.isNullOrEmpty()) return File(envCache)

    val paths = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true
    )
    val base = (paths.firstOrNull() as? String)?.takeIf { it.isNotEmpty() }
        ?: "${NSHomeDirectory()}/Library/Caches"
    return File(base, sanitizeSegment(packageName))
}

internal actual fun platformDefaultAssetsDir(): File {
    val envAssets = getenv("JAKO_ASSETS_DIR")?.toKString()?.trim()
    if (!envAssets.isNullOrEmpty()) return File(envAssets)

    val resourcePath = NSBundle.mainBundle.resourcePath?.trim()
    if (!resourcePath.isNullOrEmpty()) {
        val bundleAssets = File(resourcePath, "assets")
        if (bundleAssets.isDirectory) return bundleAssets
    }

    val exePath = NSBundle.mainBundle.executablePath?.trim()
    if (!exePath.isNullOrEmpty()) {
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

    val resourcePath = NSBundle.mainBundle.resourcePath?.trim()
    if (!resourcePath.isNullOrEmpty()) {
        val bundleRes = File(resourcePath, "res")
        if (bundleRes.isDirectory) return bundleRes
    }

    val exePath = NSBundle.mainBundle.executablePath?.trim()
    if (!exePath.isNullOrEmpty()) {
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

private fun sanitizeSegment(raw: String): String {
    val cleaned = raw.trim().replace('/', '_').replace('\\', '_').replace(':', '_')
    return if (cleaned.isEmpty() || cleaned == "." || cleaned == "..") "jako.android.app" else cleaned
}
