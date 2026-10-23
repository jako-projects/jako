@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

import java.security.cert.Certificate

expect interface SSLSession {
    fun getPeerHost(): String?
    fun getPeerPort(): Int
    /**
     * Java `getPeerCertificates()` is non-null and throws [SSLPeerUnverifiedException]
     * when the peer is not verified.
     * [Nullability Decision] Non-null `Array<Certificate>`.
     */
    fun getPeerCertificates(): Array<Certificate>
    open fun getPeerPrincipal(): Any?
    fun getCipherSuite(): String
    fun getProtocol(): String
    fun isValid(): Boolean

    open val peerHost: String?
    open val peerPort: Int
    open val peerCertificates: Array<Certificate>
    open val peerPrincipal: Any?
    open val cipherSuite: String
    open val protocol: String
    open val isValid: Boolean
}
