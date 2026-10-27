package javax.net.ssl

actual open class SSLContext private constructor(
    private val protocolName: String
) {
    actual protected constructor() : this("TLS")

    private var customTrustManager: X509TrustManager? = null

    actual open fun init(km: Array<KeyManager>?, tm: Array<TrustManager>?, sr: Any?) {
        customTrustManager = tm?.firstOrNull { it is X509TrustManager } as? X509TrustManager
    }

    actual open fun getSocketFactory(): SSLSocketFactory = NativeSSLSocketFactory(customTrustManager)
    actual open val socketFactory: SSLSocketFactory get() = getSocketFactory()

    actual open fun getProtocol(): String = protocolName
    actual open val protocol: String get() = getProtocol()

    actual companion object {
        private var defaultContext: SSLContext = SSLContext("TLS")

        actual fun getInstance(protocol: String): SSLContext = SSLContext(protocol)
        actual fun getDefault(): SSLContext = defaultContext
    }
}
