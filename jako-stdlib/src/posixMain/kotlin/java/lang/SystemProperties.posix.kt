@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.lang

import kotlinx.cinterop.IntVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
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

@OptIn(kotlinx.cinterop.UnsafeNumber::class)
internal actual fun nativeAvailableProcessors(): Int {
    val n = platform.posix.sysconf(platform.posix._SC_NPROCESSORS_ONLN).toInt()
    return if (n > 0) n else 1
}

internal actual fun nativeCreatePipe(): IntArray? {
    val fds = IntArray(2)
    val rc = fds.usePinned { pinned ->
        platform.posix.pipe(pinned.addressOf(0))
    }
    return if (rc == 0) fds else null
}

@OptIn(kotlinx.cinterop.UnsafeNumber::class)
internal actual fun nativeWriteFd(fd: Int, buf: ByteArray, off: Int, len: Int): Int {
    if (len == 0) return 0
    return buf.usePinned { pinned ->
        platform.posix.write(fd, pinned.addressOf(off), len.convert()).toInt()
    }
}

internal actual fun nativeCloseFd(fd: Int) {
    if (fd >= 0) {
        platform.posix.close(fd)
    }
}

internal actual fun nativeKillProcess(pid: Int, forcibly: Boolean) {
    if (pid > 0) {
        val sig = if (forcibly) platform.posix.SIGKILL else platform.posix.SIGTERM
        platform.posix.kill(pid, sig)
    }
}

internal actual fun nativeDecodeWaitStatus(rawStatus: Int): Int {
    if (rawStatus == -1) return -1
    val sig = rawStatus and 0x7F
    return if (sig == 0) {
        (rawStatus ushr 8) and 0xFF
    } else {
        128 + sig
    }
}

internal actual fun nativeIsWindows(): Boolean = false
internal actual fun nativeCanExecute(path: String): Boolean =
    platform.posix.access(path, platform.posix.X_OK) == 0

internal actual fun nativeRunSystemCommand(cmd: String): Int {
    val fp = platform.posix.popen(cmd, "r") ?: return -1
    return platform.posix.pclose(fp)
}

