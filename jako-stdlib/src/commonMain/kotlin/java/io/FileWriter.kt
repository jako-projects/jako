@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class FileWriter : OutputStreamWriter {
    constructor(file: File)
    constructor(fileName: String)
    constructor(fileName: String, append: Boolean)
    constructor(file: File, append: Boolean)
}
