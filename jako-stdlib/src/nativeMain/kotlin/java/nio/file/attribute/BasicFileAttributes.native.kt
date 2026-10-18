package java.nio.file.attribute

import java.io.File
import java.nio.file.NoSuchFileException
import java.nio.file.Path

actual interface BasicFileAttributes {
    actual fun lastModifiedTime(): FileTime
    actual fun lastAccessTime(): FileTime
    actual fun creationTime(): FileTime
    actual fun isRegularFile(): Boolean
    actual fun isDirectory(): Boolean
    actual fun isSymbolicLink(): Boolean
    actual fun isOther(): Boolean
    actual fun size(): Long
    actual fun fileKey(): Any?

    actual val lastModifiedTime: FileTime
    actual val lastAccessTime: FileTime
    actual val creationTime: FileTime
    actual val isRegularFile: Boolean
    actual val isDirectory: Boolean
    actual val isSymbolicLink: Boolean
    actual val isOther: Boolean
    actual val size: Long
}

actual interface FileAttribute<T> {
    actual fun name(): String
    actual fun value(): T
}

internal class NativeBasicFileAttributes(
    private val lastModified: FileTime,
    private val directory: Boolean,
    private val regularFile: Boolean,
    private val symbolicLink: Boolean,
    private val fileSize: Long,
    private val key: Any?
) : BasicFileAttributes {
    override fun lastModifiedTime(): FileTime = lastModified
    override fun lastAccessTime(): FileTime = lastModified
    override fun creationTime(): FileTime = lastModified
    override fun isRegularFile(): Boolean = regularFile
    override fun isDirectory(): Boolean = directory
    override fun isSymbolicLink(): Boolean = symbolicLink
    override fun isOther(): Boolean = !directory && !regularFile && !symbolicLink
    override fun size(): Long = fileSize
    override fun fileKey(): Any? = key

    override val lastModifiedTime: FileTime get() = lastModifiedTime()
    override val lastAccessTime: FileTime get() = lastAccessTime()
    override val creationTime: FileTime get() = creationTime()
    override val isRegularFile: Boolean get() = isRegularFile()
    override val isDirectory: Boolean get() = isDirectory()
    override val isSymbolicLink: Boolean get() = isSymbolicLink()
    override val isOther: Boolean get() = isOther()
    override val size: Long get() = size()
}

internal fun nativeReadBasicAttributes(path: Path): BasicFileAttributes {
    val file = path.toFile()
    if (!file.exists()) throw NoSuchFileException(path.toString())
    val directory = file.isDirectory()
    val regularFile = file.isFile()
    return NativeBasicFileAttributes(
        lastModified = FileTime.fromMillis(fileLastModifiedMillis(file)),
        directory = directory,
        regularFile = regularFile,
        symbolicLink = false,
        fileSize = if (regularFile || directory) file.length() else 0L,
        key = path.toAbsolutePath().toString()
    )
}

private fun fileLastModifiedMillis(file: File): Long = file.lastModified()
