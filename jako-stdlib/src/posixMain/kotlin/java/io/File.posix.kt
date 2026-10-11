@file:OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)

package java.io

import kotlinx.cinterop.*
import platform.posix.*

actual open class File : Serializable, Comparable<File> {
    private val pathString: String

    actual constructor(pathname: String) {
        this.pathString = normalizePath(pathname)
    }

    actual constructor(parent: String?, child: String) {
        this.pathString = normalizePath(joinPaths(parent ?: "", child))
    }

    actual constructor(parent: File?, child: String) {
        this.pathString = if (parent != null) {
            normalizePath(joinPaths(parent.getPath(), child))
        } else {
            normalizePath(child)
        }
    }

    actual open fun getName(): String {
        val idx = pathString.lastIndexOf('/')
        return if (idx >= 0) pathString.substring(idx + 1) else pathString
    }

    actual open fun getParent(): String? {
        val idx = pathString.lastIndexOf('/')
        if (idx <= 0) return if (pathString.startsWith("/") && pathString.length > 1) "/" else null
        return pathString.substring(0, idx)
    }

    actual open fun getParentFile(): File? {
        val p = getParent() ?: return null
        return File(p)
    }

    actual open fun getPath(): String = pathString

    actual open fun isAbsolute(): Boolean {
        return pathString.startsWith("/") || (pathString.length >= 2 && pathString[1] == ':')
    }

    actual open fun getAbsolutePath(): String {
        if (isAbsolute()) return pathString
        val cwd = NativeFileSystem.getCwd()
        return if (cwd.isEmpty()) pathString else joinPaths(cwd, pathString)
    }

    actual open fun getAbsoluteFile(): File = File(getAbsolutePath())

    @OptIn(ExperimentalForeignApi::class)
    actual open fun exists(): Boolean {
        return access(pathString, F_OK) == 0
    }

    @OptIn(ExperimentalForeignApi::class)
    actual open fun isDirectory(): Boolean = memScoped {
        val statBuf = alloc<stat>()
        if (stat(pathString, statBuf.ptr) == 0) {
            (statBuf.st_mode.convert<Int>() and S_IFMT.convert<Int>()) == S_IFDIR.convert<Int>()
        } else false
    }

    @OptIn(ExperimentalForeignApi::class)
    actual open fun isFile(): Boolean = memScoped {
        val statBuf = alloc<stat>()
        if (stat(pathString, statBuf.ptr) == 0) {
            (statBuf.st_mode.convert<Int>() and S_IFMT.convert<Int>()) == S_IFREG.convert<Int>()
        } else false
    }

    @OptIn(ExperimentalForeignApi::class)
    actual open fun length(): Long = memScoped {
        val statBuf = alloc<stat>()
        if (stat(pathString, statBuf.ptr) == 0) {
            statBuf.st_size.convert<Long>()
        } else 0L
    }

    @OptIn(ExperimentalForeignApi::class)
    actual open fun delete(): Boolean {
        if (!exists()) return false
        return if (isDirectory()) {
            rmdir(pathString) == 0
        } else {
            unlink(pathString) == 0
        }
    }

    actual open fun mkdir(): Boolean {
        return NativeFileSystem.mkdir(pathString)
    }

    actual open fun mkdirs(): Boolean {
        if (exists()) return false
        if (mkdir()) return true
        val parent = getParentFile() ?: return false
        return (parent.mkdirs() || parent.exists()) && mkdir()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual open fun list(): Array<String>? {
        if (!isDirectory()) return null
        val dir = opendir(pathString) ?: return null
        val result = mutableListOf<String>()
        try {
            while (true) {
                val entry = readdir(dir) ?: break
                val name = entry.pointed.d_name.toKString()
                if (name != "." && name != "..") {
                    result.add(name)
                }
            }
        } finally {
            closedir(dir)
        }
        return result.toTypedArray()
    }

    actual open fun listFiles(): Array<File>? {
        val names = list() ?: return null
        return Array(names.size) { i -> File(this, names[i]) }
    }

    actual open fun list(filter: FilenameFilter?): Array<String>? {
        val names = list() ?: return null
        if (filter == null) return names
        return names.filter { filter.accept(this, it) }.toTypedArray()
    }

    actual open fun listFiles(filter: FileFilter?): Array<File>? {
        val files = listFiles() ?: return null
        if (filter == null) return files
        return files.filter { filter.accept(it) }.toTypedArray()
    }

    actual open fun listFiles(filter: FilenameFilter?): Array<File>? {
        val names = list(filter) ?: return null
        return Array(names.size) { i -> File(this, names[i]) }
    }

    actual open fun lastModified(): Long = NativeFileSystem.lastModifiedMillis(pathString)

    actual open fun setLastModified(time: Long): Boolean {
        if (time < 0 || !exists()) return false
        return NativeFileSystem.setLastModifiedMillis(pathString, time)
    }

    actual open fun renameTo(dest: File): Boolean = NativeFileSystem.rename(pathString, dest.getPath())

    actual open fun canRead(): Boolean = NativeFileSystem.canAccess(pathString, R_OK)

    actual open fun canWrite(): Boolean = NativeFileSystem.canAccess(pathString, W_OK)

    actual open fun canExecute(): Boolean = NativeFileSystem.canAccess(pathString, X_OK)

    actual open fun getCanonicalPath(): String = NativeFileSystem.canonicalPath(getAbsolutePath())

    actual open fun getCanonicalFile(): File = File(getCanonicalPath())

    actual open fun createNewFile(): Boolean = NativeFileSystem.createNewFile(pathString)

    actual open fun isHidden(): Boolean = NativeFileSystem.isHiddenName(getName())

    actual override fun compareTo(other: File): Int = pathString.compareTo(other.pathString)

    actual open val name: String get() = getName()
    actual open val parent: String? get() = getParent()
    actual open val parentFile: File? get() = getParentFile()
    actual open val path: String get() = getPath()
    actual open val isAbsolute: Boolean get() = isAbsolute()
    actual open val absolutePath: String get() = getAbsolutePath()
    actual open val absoluteFile: File get() = getAbsoluteFile()
    actual open val canonicalPath: String get() = getCanonicalPath()
    actual open val canonicalFile: File get() = getCanonicalFile()
    actual open val isDirectory: Boolean get() = isDirectory()
    actual open val isFile: Boolean get() = isFile()
    actual open val isHidden: Boolean get() = isHidden()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is File) return false
        return pathString == other.pathString
    }

    override fun hashCode(): Int = pathString.hashCode()

    override fun toString(): String = pathString

    actual companion object {
        actual val separatorChar: Char = '/'
        actual val separator: String = "/"
        actual val pathSeparatorChar: Char = ':'
        actual val pathSeparator: String = ":"

        private fun normalizePath(p: String): String {
            if (p.isEmpty()) return ""
            var res = p.replace('\\', '/')
            while (res.endsWith("/") && res.length > 1) {
                res = res.substring(0, res.length - 1)
            }
            return res
        }

        private fun joinPaths(p1: String, p2: String): String {
            if (p1.isEmpty()) return p2
            if (p2.isEmpty()) return p1
            return if (p1.endsWith("/")) "$p1$p2" else "$p1/$p2"
        }

        actual fun createTempFile(prefix: String, suffix: String?, directory: File?): File {
            if (prefix.length < 3) throw IllegalArgumentException("Prefix string too short")
            val sfx = suffix ?: ".tmp"
            val targetDir = directory ?: File(java.lang.System.getProperty("java.io.tmpdir") ?: "/tmp")
            if (!targetDir.exists()) {
                throw IOException("Temporary directory does not exist: ${targetDir.getPath()}")
            }
            if (!targetDir.isDirectory()) {
                throw IOException("Not a directory: ${targetDir.getPath()}")
            }
            val baseDir = normalizePath(targetDir.getPath())
            var attempts = 0
            while (attempts < 10000) {
                attempts++
                val randomNum = kotlin.random.Random.nextLong().let { if (it < 0) -it else it }
                val candidateName = "$prefix$randomNum$sfx"
                val candidatePath = joinPaths(baseDir, candidateName)
                val file = File(candidatePath)
                if (file.createNewFile()) {
                    return file
                }
            }
            throw IOException("Unable to create temporary file after $attempts attempts")
        }

        actual fun createTempFile(prefix: String, suffix: String?): File =
            createTempFile(prefix, suffix, null)
    }
}
