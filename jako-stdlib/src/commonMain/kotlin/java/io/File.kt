@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport

expect open class File : Serializable, Comparable<File> {
    constructor(pathname: String)
    constructor(parent: String?, child: String)
    constructor(parent: File?, child: String)
    constructor(uri: java.net.URI)

    open fun getName(): String
    open fun getParent(): String?
    open fun getParentFile(): File?
    open fun getPath(): String
    open fun isAbsolute(): Boolean
    open fun getAbsolutePath(): String
    open fun getAbsoluteFile(): File
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun exists(): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun isDirectory(): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun isFile(): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun length(): Long
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun delete(): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun deleteOnExit()
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun mkdir(): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun mkdirs(): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun list(): Array<String>?
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun listFiles(): Array<File>?
    /** [Nullability Decision] Java File.list(FilenameFilter) filter is nullable; null accepts all names. */
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun list(filter: FilenameFilter?): Array<String>?
    /** [Nullability Decision] Java File.listFiles(FileFilter) filter is nullable; null accepts all files. */
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun listFiles(filter: FileFilter?): Array<File>?
    /** [Nullability Decision] Java File.listFiles(FilenameFilter) filter is nullable; null accepts all files. */
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun listFiles(filter: FilenameFilter?): Array<File>?
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun lastModified(): Long
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun setLastModified(time: Long): Boolean
    /** [Nullability Decision] Java File.renameTo(File) dest is non-null. */
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun renameTo(dest: File): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun setReadOnly(): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun setWritable(writable: Boolean): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun setReadable(readable: Boolean): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun setExecutable(executable: Boolean): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun canRead(): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun canWrite(): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun canExecute(): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun getCanonicalPath(): String
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun getCanonicalFile(): File
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun createNewFile(): Boolean
    open fun isHidden(): Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun getTotalSpace(): Long
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun getFreeSpace(): Long
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open fun getUsableSpace(): Long
    open fun toURI(): java.net.URI
    open fun toPath(): java.nio.file.Path
    override fun compareTo(other: File): Int

    open val name: String
    open val parent: String?
    open val parentFile: File?
    open val path: String
    open val isAbsolute: Boolean
    open val absolutePath: String
    open val absoluteFile: File
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open val canonicalPath: String
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open val canonicalFile: File
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open val isDirectory: Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open val isFile: Boolean
    open val isHidden: Boolean
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open val totalSpace: Long
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open val freeSpace: Long
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
    open val usableSpace: Long

    companion object {
        val separatorChar: Char
        val separator: String
        val pathSeparatorChar: Char
        val pathSeparator: String

        /** [Nullability Decision] Java File.listRoots() returns non-null Array<File>. */
        fun listRoots(): Array<File>

        /** [Nullability Decision] prefix must be >= 3 chars; suffix is nullable (defaults to ".tmp"); directory is nullable (defaults to java.io.tmpdir). */
        @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
        fun createTempFile(prefix: String, suffix: String?, directory: File?): File
        @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
        fun createTempFile(prefix: String, suffix: String?): File
    }
}
