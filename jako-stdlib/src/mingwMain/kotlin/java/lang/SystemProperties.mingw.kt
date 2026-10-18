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

internal actual fun nativeAvailableProcessors(): Int {
    val env = platform.posix.getenv("NUMBER_OF_PROCESSORS")?.toKString()?.toIntOrNull()
    return if (env != null && env > 0) env else 1
}

internal actual fun nativeCreatePipe(): IntArray? = null

internal actual fun nativeWriteFd(fd: Int, buf: ByteArray, off: Int, len: Int): Int = -1

internal actual fun nativeCloseFd(fd: Int) {}

internal actual fun nativeKillProcess(pid: Int, forcibly: Boolean) {
    if (pid > 0) {
        val flag = if (forcibly) "/F " else ""
        platform.posix.system("taskkill ${flag}/PID $pid /T >NUL 2>&1")
    }
}

internal actual fun nativeDecodeWaitStatus(rawStatus: Int): Int = rawStatus

internal actual fun nativeIsWindows(): Boolean = true

internal actual fun nativeCanExecute(path: String): Boolean = java.io.File(path).exists()

internal actual fun nativeRunSystemCommand(cmd: String): Int = platform.posix.system(cmd)

