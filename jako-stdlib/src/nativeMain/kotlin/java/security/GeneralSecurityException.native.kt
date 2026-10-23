package java.security

actual open class GeneralSecurityException : Exception {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}
