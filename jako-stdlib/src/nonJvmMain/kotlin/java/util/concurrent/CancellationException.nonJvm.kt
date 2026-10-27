package java.util.concurrent

actual open class CancellationException : IllegalStateException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}
