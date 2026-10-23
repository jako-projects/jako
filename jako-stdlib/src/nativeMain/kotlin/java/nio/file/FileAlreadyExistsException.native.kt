package java.nio.file

import java.nio.file.attribute.nativeReadBasicAttributes

actual open class FileAlreadyExistsException actual constructor(
    file: String?,
    other: String?,
    reason: String?
) : FileSystemException(file, other, reason) {
    actual constructor(file: String?) : this(file, null, null)
}
