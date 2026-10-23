@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class FileReader : InputStreamReader {
    constructor(file: File)
    constructor(fileName: String)
}
