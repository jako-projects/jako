package java.io

import kotlinx.cinterop.*
import platform.posix.*

internal object NativeFileSystem {
    @OptIn(ExperimentalForeignApi::class)
    fun getCwd(): String = memScoped {
        val buf = allocArray<ByteVar>(1024)
        val path = getcwd(buf, 1024)?.toKString() ?: ""
        path.replace('\\', '/')
    }

    @OptIn(ExperimentalForeignApi::class)
    fun mkdir(path: String): Boolean {
        return platform.posix.mkdir(path) == 0
    }

    @OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
    fun lastModifiedMillis(path: String): Long = memScoped {
        val statBuf = alloc<stat>()
        if (stat(path, statBuf.ptr) != 0) return 0L
        statBuf.st_mtime.convert<Long>() * 1000L
    }

    @OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
    fun setLastModifiedMillis(path: String, millis: Long): Boolean = memScoped {
        val buf = alloc<utimbuf>()
        val sec = (millis / 1000L).convert<platform.posix.time_t>()
        buf.actime = sec
        buf.modtime = sec
        utime(path, buf.ptr) == 0
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

    fun canonicalPath(path: String): String {
        val normalized = path.replace('\\', '/')
        val absolute = if (isAbsoluteWin(normalized)) normalized else {
            val cwd = getCwd()
            if (cwd.isEmpty()) normalized else if (cwd.endsWith("/")) "$cwd$normalized" else "$cwd/$normalized"
        }
        return collapseDotSegments(absolute)
    }

    fun isHiddenName(name: String): Boolean = name.startsWith(".")

    private fun isAbsoluteWin(path: String): Boolean {
        return path.startsWith("/") || (path.length >= 2 && path[1] == ':')
    }

    private fun collapseDotSegments(absolute: String): String {
        val prefix = if (absolute.length >= 2 && absolute[1] == ':') absolute.substring(0, 2) else ""
        val rest = if (prefix.isNotEmpty()) absolute.substring(2) else absolute
        val parts = rest.split('/').filter { it.isNotEmpty() && it != "." }
        val stack = mutableListOf<String>()
        for (part in parts) {
            if (part == "..") {
                if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
            } else {
                stack.add(part)
            }
        }
        val joined = stack.joinToString("/")
        return if (prefix.isNotEmpty()) "$prefix/$joined" else "/$joined"
    }
}
