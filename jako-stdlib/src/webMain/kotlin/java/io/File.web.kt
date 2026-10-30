package java.io

import com.bennyhuo.jako.NodeBridge
import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.currentWebRuntime
import com.bennyhuo.jako.requireWebSupport

private const val FS_UNSUPPORTED_REASON =
    "Browser sandbox does not allow synchronous local file system access"
private const val POSIX_MODE_UNSUPPORTED_REASON =
    "POSIX file mode operations require Node.js fs.chmodSync"
private const val DISK_SPACE_UNSUPPORTED_REASON =
    "Filesystem disk space query requires Node.js fs.statfsSync"

internal actual fun nativeGetPosixMode(path: String): Int {
    requireWebSupport(
        api = "java.nio.file.Files.getPosixFilePermissions(Path, ...)",
        WebRuntime.NODE_JS,
        WebRuntime.WASI,
        reason = FS_UNSUPPORTED_REASON
    )
    val st = NodeBridge.stat(path, true)
    if (st.isEmpty() || st.startsWith("ERR|")) return -1
    val parts = st.split('|')
    if (parts.size < 6) return -1
    val mode = parts[5].toIntOrNull() ?: return -1
    return mode and 0x1FF
}

internal actual fun nativeSetPosixMode(path: String, mode: Int): Boolean {
    requireWebSupport(
        api = "java.nio.file.Files.setPosixFilePermissions(Path, Set)",
        WebRuntime.NODE_JS,
        WebRuntime.WASI,
        reason = FS_UNSUPPORTED_REASON
    )
    val st = NodeBridge.stat(path, true)
    if (st.isEmpty() || st.startsWith("ERR|")) return false
    val parts = st.split('|')
    if (parts.size < 6) return false
    val currentMode = parts[5].toIntOrNull() ?: return false
    val newMode = (currentMode and 0x1FF.inv()) or (mode and 0x1FF)
    return NodeBridge.chmod(path, newMode)
}

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

    actual constructor(uri: java.net.URI) {
        if (!uri.isAbsolute()) throw IllegalArgumentException("URI is not absolute")
        if (uri.isOpaque()) throw IllegalArgumentException("URI is not hierarchical")
        val scheme = uri.getScheme()
        if (scheme == null || !scheme.equals("file", ignoreCase = true)) {
            throw IllegalArgumentException("URI scheme is not \"file\"")
        }
        if (uri.getAuthority() != null) throw IllegalArgumentException("URI has an authority component")
        if (uri.getFragment() != null) throw IllegalArgumentException("URI has a fragment component")
        if (uri.getQuery() != null) throw IllegalArgumentException("URI has a query component")
        val p = uri.getPath()
        if (p == null || p.isEmpty()) throw IllegalArgumentException("URI path component is empty")
        val winPath = if (p.length >= 3 && p[0] == '/' && p[2] == ':') p.substring(1) else p
        this.pathString = normalizePath(winPath)
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
        val cwd = (if (currentWebRuntime() == WebRuntime.BROWSER) "/" else NodeBridge.userDir()).replace('\\', '/')
        return if (cwd.isEmpty()) pathString else joinPaths(cwd, pathString)
    }

    actual open fun getAbsoluteFile(): File = File(getAbsolutePath())

    actual open fun exists(): Boolean {
        requireWebSupport("java.io.File.exists()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        if (pathString.isEmpty()) return false
        val st = NodeBridge.stat(pathString, true)
        return st.isNotEmpty() && !st.startsWith("ERR|")
    }

    actual open fun isDirectory(): Boolean {
        requireWebSupport("java.io.File.isDirectory()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        if (pathString.isEmpty()) return false
        val st = NodeBridge.stat(pathString, true)
        return st.startsWith("2|")
    }

    actual open fun isFile(): Boolean {
        requireWebSupport("java.io.File.isFile()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        if (pathString.isEmpty()) return false
        val st = NodeBridge.stat(pathString, true)
        return st.startsWith("1|")
    }

    actual open fun length(): Long {
        requireWebSupport("java.io.File.length()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        if (pathString.isEmpty()) return 0L
        val st = NodeBridge.stat(pathString, true)
        if (st.isEmpty() || st.startsWith("ERR|")) return 0L
        val parts = st.split('|')
        return if (parts.size >= 2) (parts[1].toLongOrNull() ?: 0L) else 0L
    }

    actual open fun delete(): Boolean {
        requireWebSupport("java.io.File.delete()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        if (pathString.isEmpty()) return false
        return NodeBridge.unlinkOrRmdir(pathString)
    }

    actual open fun deleteOnExit() {
        requireWebSupport("java.io.File.deleteOnExit()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        deleteOnExitPaths.add(pathString)
    }

    actual open fun mkdir(): Boolean {
        requireWebSupport("java.io.File.mkdir()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        if (pathString.isEmpty()) return false
        return NodeBridge.mkdir(pathString)
    }

    actual open fun mkdirs(): Boolean {
        requireWebSupport("java.io.File.mkdirs()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        if (exists()) return false
        if (mkdir()) return true
        val parent = getParentFile() ?: return false
        return (parent.mkdirs() || parent.exists()) && mkdir()
    }

    actual open fun list(): Array<String>? {
        requireWebSupport("java.io.File.list()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        if (!isDirectory()) return null
        val res = NodeBridge.readdir(pathString)
        if (!res.startsWith("OK\u0000")) return null
        val payload = res.substring(3)
        if (payload.isEmpty()) return emptyArray()
        return payload.split('\u0000').toTypedArray()
    }

    actual open fun listFiles(): Array<File>? {
        requireWebSupport("java.io.File.listFiles()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        val names = list() ?: return null
        return Array(names.size) { i -> File(this, names[i]) }
    }

    actual open fun list(filter: FilenameFilter?): Array<String>? {
        requireWebSupport("java.io.File.list(FilenameFilter)", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        val names = list() ?: return null
        if (filter == null) return names
        return names.filter { filter.accept(this, it) }.toTypedArray()
    }

    actual open fun listFiles(filter: FileFilter?): Array<File>? {
        requireWebSupport("java.io.File.listFiles(FileFilter)", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        val files = listFiles() ?: return null
        if (filter == null) return files
        return files.filter { filter.accept(it) }.toTypedArray()
    }

    actual open fun listFiles(filter: FilenameFilter?): Array<File>? {
        requireWebSupport("java.io.File.listFiles(FilenameFilter)", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        val names = list(filter) ?: return null
        return Array(names.size) { i -> File(this, names[i]) }
    }

    actual open fun lastModified(): Long {
        requireWebSupport("java.io.File.lastModified()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        if (pathString.isEmpty()) return 0L
        val st = NodeBridge.stat(pathString, true)
        if (st.isEmpty() || st.startsWith("ERR|")) return 0L
        val parts = st.split('|')
        return if (parts.size >= 3) (parts[2].toLongOrNull() ?: 0L) else 0L
    }

    actual open fun setLastModified(time: Long): Boolean {
        requireWebSupport("java.io.File.setLastModified(Long)", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        if (time < 0 || !exists()) return false
        return NodeBridge.utimes(pathString, time.toDouble(), time.toDouble())
    }

    actual open fun renameTo(dest: File): Boolean {
        requireWebSupport("java.io.File.renameTo(File)", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        return NodeBridge.rename(pathString, dest.getPath())
    }

    private fun updatePermissions(mask: Int, enable: Boolean): Boolean {
        requireWebSupport("java.io.File.setPermissions", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        val currentMode = nativeGetPosixMode(pathString)
        if (currentMode < 0) return false
        val newMode = if (enable) currentMode or mask else currentMode and mask.inv()
        return nativeSetPosixMode(pathString, newMode)
    }

    actual open fun setReadOnly(): Boolean = updatePermissions(146, false)

    actual open fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        val mask = if (ownerOnly) 128 else 146
        return updatePermissions(mask, writable)
    }

    actual open fun setWritable(writable: Boolean): Boolean = setWritable(writable, true)

    actual open fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        val mask = if (ownerOnly) 256 else 292
        return updatePermissions(mask, readable)
    }

    actual open fun setReadable(readable: Boolean): Boolean = setReadable(readable, true)

    actual open fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        val mask = if (ownerOnly) 64 else 73
        return updatePermissions(mask, executable)
    }

    actual open fun setExecutable(executable: Boolean): Boolean = setExecutable(executable, true)

    actual open fun canRead(): Boolean {
        requireWebSupport("java.io.File.canRead()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        return exists() && NodeBridge.access(pathString, 4)
    }

    actual open fun canWrite(): Boolean {
        requireWebSupport("java.io.File.canWrite()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        return exists() && NodeBridge.access(pathString, 2)
    }

    actual open fun canExecute(): Boolean {
        requireWebSupport("java.io.File.canExecute()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        return exists() && NodeBridge.access(pathString, 1)
    }

    actual open fun getCanonicalPath(): String {
        requireWebSupport("java.io.File.getCanonicalPath()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        return NodeBridge.realpath(getAbsolutePath()).replace('\\', '/')
    }

    actual open fun getCanonicalFile(): File = File(getCanonicalPath())

    actual open fun createNewFile(): Boolean {
        requireWebSupport("java.io.File.createNewFile()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        if (pathString.isEmpty()) throw IOException("No such file or directory")
        if (exists()) return false
        val fd = NodeBridge.open(pathString, "w")
        if (fd >= 0) {
            NodeBridge.close(fd)
            return true
        }
        throw IOException("Unable to create file: $pathString (${NodeBridge.lastError()})")
    }

    actual open fun isHidden(): Boolean = getName().startsWith(".")

    actual open fun getTotalSpace(): Long {
        requireWebSupport("java.io.File.getTotalSpace()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        return if (exists()) NodeBridge.diskSpace(pathString, 0).toLong() else 0L
    }

    actual open fun getFreeSpace(): Long {
        requireWebSupport("java.io.File.getFreeSpace()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        return if (exists()) NodeBridge.diskSpace(pathString, 1).toLong() else 0L
    }

    actual open fun getUsableSpace(): Long {
        requireWebSupport("java.io.File.getUsableSpace()", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
        return if (exists()) NodeBridge.diskSpace(pathString, 2).toLong() else 0L
    }

    actual open fun toURI(): java.net.URI {
        val f = getAbsoluteFile()
        var sp = f.getPath().replace('\\', '/')
        if (!sp.startsWith("/")) sp = "/$sp"
        if (!sp.endsWith("/") && f.isDirectory()) sp = "$sp/"
        return java.net.URI("file", null, sp, null, null)
    }

    actual open fun toPath(): java.nio.file.Path = java.nio.file.Paths.get(pathString)

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
    actual open val totalSpace: Long get() = getTotalSpace()
    actual open val freeSpace: Long get() = getFreeSpace()
    actual open val usableSpace: Long get() = getUsableSpace()

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
        private val deleteOnExitPaths = linkedSetOf<String>()

        actual fun listRoots(): Array<File> = arrayOf(File("/"))

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
            requireWebSupport("java.io.File.createTempFile(String, String?, File?)", WebRuntime.NODE_JS, WebRuntime.WASI, reason = FS_UNSUPPORTED_REASON)
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
