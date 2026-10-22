package java.io

actual open class UnsupportedEncodingException : IOException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}
