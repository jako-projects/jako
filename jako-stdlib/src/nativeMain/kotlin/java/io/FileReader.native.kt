package java.io

actual open class FileReader : InputStreamReader {
    actual constructor(fileName: String) : super(FileInputStream(fileName))
    actual constructor(file: File) : super(FileInputStream(file))
}
