package javax.net.ssl

actual open class SSLHandshakeException actual constructor(reason: String?) : SSLException(reason)
