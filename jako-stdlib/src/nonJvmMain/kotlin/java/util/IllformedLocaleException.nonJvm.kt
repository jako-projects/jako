package java.util

actual open class IllformedLocaleException : RuntimeException {
    private val errIdx: Int

    actual constructor() : super() {
        errIdx = -1
    }

    actual constructor(message: String?) : super(message) {
        errIdx = -1
    }

    actual constructor(message: String?, errorIndex: Int) : super(
        (message ?: "") + (if (errorIndex < 0) "" else " [at index $errorIndex]")
    ) {
        errIdx = errorIndex
    }

    actual open fun getErrorIndex(): Int = errIdx
    actual open val errorIndex: Int
        get() = errIdx
}
