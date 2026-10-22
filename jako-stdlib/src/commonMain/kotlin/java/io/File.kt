@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

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
    open fun exists(): Boolean
    open fun isDirectory(): Boolean
    open fun isFile(): Boolean
    open fun length(): Long
    open fun delete(): Boolean
    open fun deleteOnExit()
    open fun mkdir(): Boolean
    open fun mkdirs(): Boolean
    open fun list(): Array<String>?
    open fun listFiles(): Array<File>?
    /** [Nullability Decision] Java File.list(FilenameFilter) filter is nullable; null accepts all names. */
    open fun list(filter: FilenameFilter?): Array<String>?
    /** [Nullability Decision] Java File.listFiles(FileFilter) filter is nullable; null accepts all files. */
    open fun listFiles(filter: FileFilter?): Array<File>?
    /** [Nullability Decision] Java File.listFiles(FilenameFilter) filter is nullable; null accepts all files. */
    open fun listFiles(filter: FilenameFilter?): Array<File>?
    open fun lastModified(): Long
    open fun setLastModified(time: Long): Boolean
    /** [Nullability Decision] Java File.renameTo(File) dest is non-null. */
    open fun renameTo(dest: File): Boolean
    open fun setReadOnly(): Boolean
    open fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean
    open fun setWritable(writable: Boolean): Boolean
    open fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean
    open fun setReadable(readable: Boolean): Boolean
    open fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean
    open fun setExecutable(executable: Boolean): Boolean
    open fun canRead(): Boolean
    open fun canWrite(): Boolean
    open fun canExecute(): Boolean
    open fun getCanonicalPath(): String
    open fun getCanonicalFile(): File
    open fun createNewFile(): Boolean
    open fun isHidden(): Boolean
    open fun getTotalSpace(): Long
    open fun getFreeSpace(): Long
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
    open val canonicalPath: String
    open val canonicalFile: File
    open val isDirectory: Boolean
    open val isFile: Boolean
    open val isHidden: Boolean
    open val totalSpace: Long
    open val freeSpace: Long
    open val usableSpace: Long

    companion object {
        val separatorChar: Char
        val separator: String
        val pathSeparatorChar: Char
        val pathSeparator: String

        /** [Nullability Decision] Java File.listRoots() returns non-null Array<File>. */
        fun listRoots(): Array<File>

        /** [Nullability Decision] prefix must be >= 3 chars; suffix is nullable (defaults to ".tmp"); directory is nullable (defaults to java.io.tmpdir). */
        fun createTempFile(prefix: String, suffix: String?, directory: File?): File
        fun createTempFile(prefix: String, suffix: String?): File
    }
}
