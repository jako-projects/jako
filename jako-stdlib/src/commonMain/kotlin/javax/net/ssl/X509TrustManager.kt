@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

import java.security.cert.X509Certificate

expect interface X509TrustManager : TrustManager {
    fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?)
    fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?)
    fun getAcceptedIssuers(): Array<X509Certificate>

    open val acceptedIssuers: Array<X509Certificate>
}
