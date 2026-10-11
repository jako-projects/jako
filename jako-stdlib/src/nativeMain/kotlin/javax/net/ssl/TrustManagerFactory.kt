package javax.net.ssl

import java.security.KeyStore

actual open class TrustManagerFactory protected constructor() {
    actual open fun init(ks: KeyStore?) {}
    actual open fun getTrustManagers(): Array<TrustManager>? = trustManagers
    actual open val trustManagers: Array<TrustManager>? get() = arrayOf(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = emptyArray()
        override val acceptedIssuers: Array<java.security.cert.X509Certificate> get() = getAcceptedIssuers()
    })

    actual companion object {
        actual fun getDefaultAlgorithm(): String = "PKIX"
        actual fun getInstance(algorithm: String): TrustManagerFactory = TrustManagerFactory()
    }
}
