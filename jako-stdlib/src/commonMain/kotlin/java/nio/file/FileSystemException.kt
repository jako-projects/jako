@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

expect open class FileSystemException : java.io.IOException {
    constructor(file: String?)
    constructor(file: String?, other: String?, reason: String?)
    val file: String?
    val otherFile: String?
    val reason: String?
}
