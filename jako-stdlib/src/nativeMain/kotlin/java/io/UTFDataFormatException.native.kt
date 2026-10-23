package java.io

actual open class UTFDataFormatException : IOException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}
