package java.lang

actual open class SecurityException : RuntimeException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}
