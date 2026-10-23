package java.net

import java.io.InterruptedIOException

actual open class SocketTimeoutException : InterruptedIOException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}
