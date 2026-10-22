@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

expect enum class StandardCopyOption : CopyOption {
    REPLACE_EXISTING,
    COPY_ATTRIBUTES,
    ATOMIC_MOVE
}
