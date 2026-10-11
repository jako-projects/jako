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
