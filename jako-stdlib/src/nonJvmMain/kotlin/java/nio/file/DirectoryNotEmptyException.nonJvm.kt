package java.nio.file

import java.nio.file.attribute.nativeReadBasicAttributes

actual open class DirectoryNotEmptyException actual constructor(
    dir: String?
) : FileSystemException(dir)
