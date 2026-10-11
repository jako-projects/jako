@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.lang

import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.posix.gettimeofday
import platform.posix.timeval

internal actual fun nativeLineSeparator(): String = "\n"
internal actual fun nativeFileSeparator(): String = "/"
internal actual fun nativePathSeparator(): String = ":"

internal actual fun nativeCurrentTimeMillis(): Long = memScoped {
    val tv = alloc<timeval>()
    gettimeofday(tv.ptr, null)
    tv.tv_sec.convert<Long>() * 1000L + tv.tv_usec.convert<Long>() / 1000L
}

internal actual fun nativeTmpDir(): String =
    platform.posix.getenv("TMPDIR")?.toKString()
        ?: platform.posix.getenv("TEMP")?.toKString()
        ?: platform.posix.getenv("TMP")?.toKString()
        ?: "/tmp"
