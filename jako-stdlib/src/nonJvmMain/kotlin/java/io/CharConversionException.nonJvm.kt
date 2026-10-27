package java.io

actual open class CharConversionException : IOException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}
