package javax.net.ssl

actual abstract class ExtendedSSLSession actual constructor() : SSLSession {
    actual open fun getRequestedServerNames(): List<Any>? = null
    actual open val requestedServerNames: List<Any>? get() = getRequestedServerNames()
}
