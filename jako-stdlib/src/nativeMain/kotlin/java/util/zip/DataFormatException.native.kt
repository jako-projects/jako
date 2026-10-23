package java.util.zip

actual open class DataFormatException : Exception {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}
