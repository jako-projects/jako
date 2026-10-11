@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

import java.security.KeyStore

expect open class TrustManagerFactory {
    open fun init(ks: KeyStore?)
    open fun getTrustManagers(): Array<TrustManager>?
    open val trustManagers: Array<TrustManager>?

    companion object {
        fun getDefaultAlgorithm(): String
        fun getInstance(algorithm: String): TrustManagerFactory
    }
}
