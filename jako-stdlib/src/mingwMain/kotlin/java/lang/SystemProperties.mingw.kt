@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.lang

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.windows.FILETIME
import platform.windows.GetSystemTimeAsFileTime
import platform.windows.ULARGE_INTEGER

internal actual fun nativeLineSeparator(): String = "\r\n"
internal actual fun nativeFileSeparator(): String = "\\"
internal actual fun nativePathSeparator(): String = ";"

internal actual fun nativeCurrentTimeMillis(): Long = memScoped {
    val ft = alloc<FILETIME>()
    GetSystemTimeAsFileTime(ft.ptr)
    val uli = alloc<ULARGE_INTEGER>()
    uli.LowPart = ft.dwLowDateTime
    uli.HighPart = ft.dwHighDateTime
    val fileTime100ns = uli.QuadPart.toLong()
    val epoch100ns = fileTime100ns - 116444736000000000L
    epoch100ns / 10_000L
}

internal actual fun nativeTmpDir(): String =
    platform.posix.getenv("TEMP")?.toKString()
        ?: platform.posix.getenv("TMP")?.toKString()
        ?: "C:\\Windows\\Temp"
