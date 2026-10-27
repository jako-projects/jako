package android.content

import java.io.File
import java.lang.System

internal actual fun platformDefaultPackageName(): String {
    val envPkg = System.getenv("JAKO_PACKAGE_NAME")?.trim()
    if (!envPkg.isNullOrEmpty()) return sanitizeSegment(envPkg)
    return "jako.android.app"
}

internal actual fun platformAppDataDir(packageName: String): File {
    val envData = System.getenv("JAKO_DATA_DIR")?.trim()
    if (!envData.isNullOrEmpty()) return File(envData)

    val home = System.getProperty("user.home")?.takeIf { it.isNotEmpty() }
        ?: System.getProperty("java.io.tmpdir")
        ?: "/tmp"
    val osName = (System.getProperty("os.name") ?: "").lowercase()
    val base = when {
        osName.contains("mac") || osName.contains("darwin") -> "$home/Library/Application Support"
        osName.contains("win") -> System.getenv("LOCALAPPDATA") ?: "$home/AppData/Local"
        else -> System.getenv("XDG_DATA_HOME")?.takeIf { it.isNotEmpty() } ?: "$home/.local/share"
    }
    return File(base, sanitizeSegment(packageName))
}

internal actual fun platformAppCacheDir(packageName: String): File {
    val envCache = System.getenv("JAKO_CACHE_DIR")?.trim()
    if (!envCache.isNullOrEmpty()) return File(envCache)

    val home = System.getProperty("user.home")?.takeIf { it.isNotEmpty() }
        ?: System.getProperty("java.io.tmpdir")
        ?: "/tmp"
    val osName = (System.getProperty("os.name") ?: "").lowercase()
    val base = when {
        osName.contains("mac") || osName.contains("darwin") -> "$home/Library/Caches"
        osName.contains("win") -> "${System.getenv("LOCALAPPDATA") ?: "$home/AppData/Local"}/Cache"
        else -> System.getenv("XDG_CACHE_HOME")?.takeIf { it.isNotEmpty() } ?: "$home/.cache"
    }
    return File(base, sanitizeSegment(packageName))
}

internal actual fun platformDefaultAssetsDir(): File {
    val envAssets = System.getenv("JAKO_ASSETS_DIR")?.trim()
    if (!envAssets.isNullOrEmpty()) return File(envAssets)
    return File("assets")
}

internal actual fun platformDefaultResourcesDir(): File {
    val envRes = System.getenv("JAKO_RESOURCES_DIR")?.trim()
    if (!envRes.isNullOrEmpty()) return File(envRes)
    return File("res")
}

internal actual fun platformDefaultExternalStorageDir(packageName: String): File {
    val envExt = System.getenv("JAKO_EXTERNAL_STORAGE_DIR")?.trim()
    if (!envExt.isNullOrEmpty()) return File(envExt)
    return File(platformAppDataDir(packageName), "external_storage")
}

private fun sanitizeSegment(raw: String): String {
    val cleaned = raw.trim().replace('/', '_').replace('\\', '_').replace(':', '_')
    return if (cleaned.isEmpty() || cleaned == "." || cleaned == "..") "jako.android.app" else cleaned
}
