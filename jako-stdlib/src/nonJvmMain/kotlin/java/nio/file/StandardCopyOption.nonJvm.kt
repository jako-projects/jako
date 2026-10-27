package java.nio.file

actual enum class StandardCopyOption : CopyOption {
    REPLACE_EXISTING,
    COPY_ATTRIBUTES,
    ATOMIC_MOVE
}
