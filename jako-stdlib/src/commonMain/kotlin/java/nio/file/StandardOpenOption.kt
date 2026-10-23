@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

expect enum class StandardOpenOption : OpenOption {
    READ,
    WRITE,
    APPEND,
    TRUNCATE_EXISTING,
    CREATE,
    CREATE_NEW,
    DELETE_ON_CLOSE,
    SPARSE,
    SYNC,
    DSYNC
}
