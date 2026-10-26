package javax.net.ssl

actual abstract class ExtendedSSLSession actual constructor() : SSLSession {
    actual open fun getRequestedServerNames(): List<Any>? {
        throw UnsupportedOperationException("Underlying provider does not implement the method")
    }
    actual open val requestedServerNames: List<Any>? get() = getRequestedServerNames()
}
