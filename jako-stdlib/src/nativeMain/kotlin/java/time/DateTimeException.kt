package java.time

actual open class DateTimeException : RuntimeException {
    actual constructor(message: String?) : super(message)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
}
