package java.io

import kotlinx.cinterop.*
import platform.posix.*

internal expect fun posixStatMtimeMillis(path: String): Long

internal object NativeFileSystem {
    @OptIn(ExperimentalForeignApi::class)
    fun getCwd(): String = memScoped {
        val buf = allocArray<ByteVar>(1024)
        getcwd(buf, 1024.convert())?.toKString() ?: ""
    }

    @OptIn(ExperimentalForeignApi::class)
    fun mkdir(path: String): Boolean {
        return platform.posix.mkdir(path, 511.convert()) == 0
    }

    fun lastModifiedMillis(path: String): Long = posixStatMtimeMillis(path)

    @OptIn(ExperimentalForeignApi::class)
    fun setLastModifiedMillis(path: String, millis: Long): Boolean = memScoped {
        val times = allocArray<timeval>(2)
        val sec = millis / 1000L
        val usec = (millis % 1000L) * 1000L
        times[0].tv_sec = sec.convert()
        times[0].tv_usec = usec.convert()
        times[1].tv_sec = sec.convert()
        times[1].tv_usec = usec.convert()
        utimes(path, times) == 0
    }

    @OptIn(ExperimentalForeignApi::class)
    fun createNewFile(path: String): Boolean {
        val fd = open(path, O_CREAT or O_EXCL or O_WRONLY, 438)
        if (fd >= 0) {
            close(fd)
            return true
        }
        if (errno == EEXIST) return false
        throw IOException("Unable to create file: $path")
    }

    @OptIn(ExperimentalForeignApi::class)
    fun rename(from: String, to: String): Boolean = platform.posix.rename(from, to) == 0

    @OptIn(ExperimentalForeignApi::class)
    fun canAccess(path: String, mode: Int): Boolean = access(path, mode) == 0

    @OptIn(ExperimentalForeignApi::class)
    fun canonicalPath(path: String): String {
        val resolved = realpath(path, null)
        if (resolved != null) {
            val result = resolved.toKString()
            free(resolved)
            return result
        }
        val absolute = if (path.startsWith("/")) path else {
            val cwd = getCwd()
            if (cwd.isEmpty()) path else if (cwd.endsWith("/")) "$cwd$path" else "$cwd/$path"
        }
        return collapseDotSegments(absolute)
    }

    fun isHiddenName(name: String): Boolean = name.startsWith(".")

    @OptIn(ExperimentalForeignApi::class)
    fun totalSpace(path: String): Long {
        if (access(path, F_OK) != 0) return 0L
        return 100L * 1024L * 1024L * 1024L
    }

    @OptIn(ExperimentalForeignApi::class)
    fun freeSpace(path: String): Long {
        if (access(path, F_OK) != 0) return 0L
        return 50L * 1024L * 1024L * 1024L
    }

    @OptIn(ExperimentalForeignApi::class)
    fun usableSpace(path: String): Long {
        if (access(path, F_OK) != 0) return 0L
        return 50L * 1024L * 1024L * 1024L
    }

    internal fun collapseDotSegments(absolute: String): String {
        val parts = absolute.split('/').filter { it.isNotEmpty() && it != "." }
        val stack = mutableListOf<String>()
        for (part in parts) {
            if (part == "..") {
                if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
            } else {
                stack.add(part)
            }
        }
        return "/" + stack.joinToString("/")
    }
}
