package java.nio.file

actual interface OpenOption

actual enum class StandardOpenOption : OpenOption {
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

actual interface CopyOption

actual enum class StandardCopyOption : CopyOption {
    REPLACE_EXISTING,
    COPY_ATTRIBUTES,
    ATOMIC_MOVE
}

actual enum class LinkOption : OpenOption, CopyOption {
    NOFOLLOW_LINKS
}
