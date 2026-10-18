package java.util

import java.io.IOException

actual typealias NoSuchElementException = kotlin.NoSuchElementException

actual typealias ConcurrentModificationException = kotlin.ConcurrentModificationException

actual open class EmptyStackException actual constructor() : RuntimeException()

actual open class TooManyListenersException : Exception {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}

actual open class MissingResourceException actual constructor(
    s: String?,
    className: String?,
    key: String?
) : RuntimeException(s) {
    private val classNameVal: String? = className
    private val keyVal: String? = key

    actual open fun getClassName(): String? = classNameVal
    actual open val className: String?
        get() = classNameVal

    actual open fun getKey(): String? = keyVal
    actual open val key: String?
        get() = keyVal
}

actual open class InvalidPropertiesFormatException : IOException {
    actual constructor(cause: Throwable?) : super(cause?.toString(), cause)
    actual constructor(message: String?) : super(message)
}

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
