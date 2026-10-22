@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

expect enum class FileVisitResult {
    CONTINUE,
    TERMINATE,
    SKIP_SUBTREE,
    SKIP_SIBLINGS
}
