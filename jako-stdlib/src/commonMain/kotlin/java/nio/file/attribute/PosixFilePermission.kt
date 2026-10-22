@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file.attribute

expect enum class PosixFilePermission {
    OWNER_READ,
    OWNER_WRITE,
    OWNER_EXECUTE,
    GROUP_READ,
    GROUP_WRITE,
    GROUP_EXECUTE,
    OTHERS_READ,
    OTHERS_WRITE,
    OTHERS_EXECUTE
}
