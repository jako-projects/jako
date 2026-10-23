@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

expect open class NotDirectoryException : FileSystemException {
    constructor(file: String?)
}
