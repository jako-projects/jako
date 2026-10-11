package java.io

actual open class FileWriter : OutputStreamWriter {
    actual constructor(fileName: String) : super(FileOutputStream(fileName))
    actual constructor(fileName: String, append: Boolean) : super(FileOutputStream(fileName, append))
    actual constructor(file: File) : super(FileOutputStream(file))
    actual constructor(file: File, append: Boolean) : super(FileOutputStream(file, append))
}
