@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.UnsafeNumber::class)

package java.io

import kotlinx.cinterop.*
import platform.posix.*

internal actual fun posixStatMtimeMillis(path: String): Long = memScoped {
    val statBuf = alloc<stat>()
    if (platform.posix.stat(path, statBuf.ptr) != 0) return 0L
    val sec = statBuf.st_mtim.tv_sec.convert<Long>()
    val nsec = statBuf.st_mtim.tv_nsec.convert<Long>()
    sec * 1000L + nsec / 1_000_000L
}
