package java.nio.file

import java.nio.file.attribute.nativeReadBasicAttributes

actual open class NotDirectoryException actual constructor(
    file: String?
) : FileSystemException(file)
