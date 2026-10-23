package java.util.concurrent

actual open class CompletionException : RuntimeException {
    protected actual constructor() : super()
    protected actual constructor(message: String?) : super(message)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}
