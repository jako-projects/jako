@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file.attribute

expect interface BasicFileAttributes {
    fun lastModifiedTime(): FileTime
    fun lastAccessTime(): FileTime
    fun creationTime(): FileTime
    fun isRegularFile(): Boolean
    fun isDirectory(): Boolean
    fun isSymbolicLink(): Boolean
    fun isOther(): Boolean
    fun size(): Long
    // [Nullability Decision] Java fileKey() is Object; providers may return null when no identity key exists.
    fun fileKey(): Any?

    val lastModifiedTime: FileTime
    val lastAccessTime: FileTime
    val creationTime: FileTime
    val isRegularFile: Boolean
    val isDirectory: Boolean
    val isSymbolicLink: Boolean
    val isOther: Boolean
    val size: Long
}

expect interface FileAttribute<T> {
    fun name(): String
    fun value(): T
}
