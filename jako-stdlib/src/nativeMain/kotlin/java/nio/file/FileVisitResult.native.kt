package java.nio.file

import java.nio.file.attribute.nativeReadBasicAttributes

actual enum class FileVisitResult {
    CONTINUE,
    TERMINATE,
    SKIP_SUBTREE,
    SKIP_SIBLINGS
}
