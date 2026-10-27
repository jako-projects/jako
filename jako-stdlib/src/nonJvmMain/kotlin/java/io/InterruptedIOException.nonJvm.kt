package java.io

actual open class InterruptedIOException : IOException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}
