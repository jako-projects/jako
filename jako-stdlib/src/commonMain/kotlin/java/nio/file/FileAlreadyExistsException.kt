@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

expect open class FileAlreadyExistsException : FileSystemException {
    constructor(file: String?)
    constructor(file: String?, other: String?, reason: String?)
}
