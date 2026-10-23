@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package android.content

import java.io.File
import java.io.FileInputStream
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import platform.posix.getenv
import platform.posix.getpwuid
import platform.posix.getuid
import platform.posix.readlink

internal actual fun platformDefaultPackageName(): String {
    val envPkg = getenv("JAKO_PACKAGE_NAME")?.toKString()?.trim()
    if (!envPkg.isNullOrEmpty()) return sanitizeSegment(envPkg)

    val commFile = File("/proc/self/comm")
    if (commFile.exists()) {
        try {
            FileInputStream(commFile).use { fis ->
                val bytes = ByteArray(128)
                val n = fis.read(bytes)
                if (n > 0) {
                    val comm = bytes.decodeToString(0, n).trim().removeSuffix(".kexe")
                    if (comm.isNotEmpty() && comm != "test") {
                        return sanitizeSegment(comm)
                    }
                }
            }
        } catch (_: Throwable) {
        }
    }
    return "jako.android.app"
}

internal actual fun platformAppDataDir(packageName: String): File {
    val envData = getenv("JAKO_DATA_DIR")?.toKString()?.trim()
    if (!envData.isNullOrEmpty()) return File(envData)

    val pkg = sanitizeSegment(packageName)
    val xdgDataHome = getenv("XDG_DATA_HOME")?.toKString()?.trim()
    if (!xdgDataHome.isNullOrEmpty() && xdgDataHome.startsWith("/")) {
        return File(xdgDataHome, pkg)
    }
    val home = resolveLinuxHomeDir()
    return File(File(home, ".local/share"), pkg)
}

internal actual fun platformAppCacheDir(packageName: String): File {
    val envCache = getenv("JAKO_CACHE_DIR")?.toKString()?.trim()
    if (!envCache.isNullOrEmpty()) return File(envCache)

    val pkg = sanitizeSegment(packageName)
    val xdgCacheHome = getenv("XDG_CACHE_HOME")?.toKString()?.trim()
    if (!xdgCacheHome.isNullOrEmpty() && xdgCacheHome.startsWith("/")) {
        return File(xdgCacheHome, pkg)
    }
    val home = resolveLinuxHomeDir()
    return File(File(home, ".cache"), pkg)
}

internal actual fun platformDefaultAssetsDir(): File {
    val envAssets = getenv("JAKO_ASSETS_DIR")?.toKString()?.trim()
    if (!envAssets.isNullOrEmpty()) return File(envAssets)

    val exeDir = resolveLinuxExecutableDir()
    if (exeDir != null) {
        val siblingAssets = File(exeDir, "assets")
        if (siblingAssets.isDirectory) return siblingAssets
    }

    return File("assets")
}

internal actual fun platformDefaultResourcesDir(): File {
    val envRes = getenv("JAKO_RESOURCES_DIR")?.toKString()?.trim()
    if (!envRes.isNullOrEmpty()) return File(envRes)

    val exeDir = resolveLinuxExecutableDir()
    if (exeDir != null) {
        val siblingRes = File(exeDir, "res")
        if (siblingRes.isDirectory) return siblingRes
    }

    return File("res")
}

internal actual fun platformDefaultExternalStorageDir(packageName: String): File {
    val envExt = getenv("JAKO_EXTERNAL_STORAGE_DIR")?.toKString()?.trim()
    if (!envExt.isNullOrEmpty()) return File(envExt)

    return File(platformAppDataDir(packageName), "external_storage")
}

private fun resolveLinuxExecutableDir(): File? {
    return try {
        memScoped {
            val buf = allocArray<ByteVar>(4096)
            val len = readlink("/proc/self/exe", buf, 4095u)
            if (len > 0) {
                buf[len.toInt()] = 0
                File(buf.toKString()).parentFile
            } else {
                null
            }
        }
    } catch (_: Throwable) {
        null
    }
}

private fun resolveLinuxHomeDir(): String {
    val envHome = getenv("HOME")?.toKString()?.trim()
    if (!envHome.isNullOrEmpty()) return envHome
    val pwDir = getpwuid(getuid())?.pointed?.pw_dir?.toKString()?.trim()
    if (!pwDir.isNullOrEmpty()) return pwDir
    return "."
}

private fun sanitizeSegment(raw: String): String {
    val cleaned = raw.trim().replace('/', '_').replace('\\', '_').replace(':', '_')
    return if (cleaned.isEmpty() || cleaned == "." || cleaned == "..") "jako.android.app" else cleaned
}
