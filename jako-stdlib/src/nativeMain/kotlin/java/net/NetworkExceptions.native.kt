package java.net

import java.io.IOException
import java.io.InterruptedIOException

actual open class SocketException : IOException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}

actual open class UnknownHostException : IOException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}

actual open class ConnectException : SocketException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}

actual open class SocketTimeoutException : InterruptedIOException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}

actual open class ProtocolException : IOException {
    actual constructor() : super()
    actual constructor(host: String?) : super(host)
}

actual open class BindException : SocketException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}

actual open class NoRouteToHostException : SocketException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}

actual open class PortUnreachableException : SocketException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}

actual open class HttpRetryException : IOException {
    private val responseCodeVal: Int
    private val locationVal: String?

    actual constructor(detail: String?, code: Int) : this(detail, code, null)

    actual constructor(detail: String?, code: Int, location: String?) : super(detail) {
        this.responseCodeVal = code
        this.locationVal = location
    }

    actual open fun responseCode(): Int = responseCodeVal

    actual open fun getReason(): String? = message

    actual open val reason: String?
        get() = message

    actual open fun getLocation(): String? = locationVal

    actual open val location: String?
        get() = locationVal
}

