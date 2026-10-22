package java.nio.file.attribute

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
