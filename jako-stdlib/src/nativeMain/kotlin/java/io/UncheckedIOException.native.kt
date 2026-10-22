package java.io

actual open class UncheckedIOException : RuntimeException {
    private val ioCause: IOException

    actual constructor(message: String?, cause: IOException) : super(message, cause) {
        this.ioCause = cause
    }

    actual constructor(cause: IOException) : super(cause) {
        this.ioCause = cause
    }

    actual override val cause: IOException?
        get() = ioCause
}
