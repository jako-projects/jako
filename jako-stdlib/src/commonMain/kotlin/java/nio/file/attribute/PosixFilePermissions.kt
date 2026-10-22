@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file.attribute

expect class PosixFilePermissions {
    companion object {
        fun toString(perms: Set<PosixFilePermission>): String
        fun fromString(perms: String): MutableSet<PosixFilePermission>
        fun asFileAttribute(perms: Set<PosixFilePermission>): FileAttribute<Set<PosixFilePermission>>
    }
}
