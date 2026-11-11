package java.util.concurrent

actual open class ExecutionException : Exception {
    actual protected constructor() : super()
    actual protected constructor(message: String?) : super(message)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}
