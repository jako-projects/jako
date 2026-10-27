package java.util

import java.io.IOException

actual open class InvalidPropertiesFormatException : IOException {
    actual constructor(cause: Throwable?) : super(cause?.toString(), cause)
    actual constructor(message: String?) : super(message)
}
