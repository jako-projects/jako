package javax.net.ssl

import java.security.cert.X509Certificate

actual interface X509TrustManager : TrustManager {
    actual fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?)
    actual fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?)
    actual fun getAcceptedIssuers(): Array<X509Certificate>
    actual open val acceptedIssuers: Array<X509Certificate> get() = getAcceptedIssuers()
}
