package java.util.concurrent

actual open class TimeoutException : Exception {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}
