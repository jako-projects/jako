package javax.net.ssl

import java.io.IOException

actual open class SSLException : IOException {
    actual constructor(reason: String?) : super(reason)
    actual constructor(reason: String?, cause: Throwable?) : super(reason, cause)
    actual constructor(cause: Throwable?) : super(cause)
}
