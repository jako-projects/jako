@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

import java.io.IOException
import java.net.Socket
import java.net.InetAddress
import javax.net.SocketFactory
import java.security.cert.Certificate
import java.security.cert.X509Certificate

expect interface SSLSession {
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

    open val peerCertificates: Array<Certificate>
    open val peerPrincipal: Any?
    open val cipherSuite: String
    open val protocol: String
    open val isValid: Boolean
}

expect abstract class ExtendedSSLSession() : SSLSession {
    open fun getRequestedServerNames(): List<Any>?
    open val requestedServerNames: List<Any>?
}

expect class SNIHostName(asciiName: String) {
    fun getAsciiName(): String
    val asciiName: String
}

expect abstract class SSLSocket : Socket {
    protected constructor()

    abstract fun startHandshake()
    abstract fun getSession(): SSLSession
    abstract fun setUseClientMode(mode: Boolean)
    abstract fun getUseClientMode(): Boolean
    open fun setNeedClientAuth(need: Boolean)
    open fun getNeedClientAuth(): Boolean
    open fun setWantClientAuth(want: Boolean)
    open fun getWantClientAuth(): Boolean
    abstract fun getEnabledProtocols(): Array<String>
    abstract fun setEnabledProtocols(protocols: Array<String>?)
    abstract fun getSupportedCipherSuites(): Array<String>
    abstract fun getEnabledCipherSuites(): Array<String>
    abstract fun setEnabledCipherSuites(suites: Array<String>?)

    open var useClientMode: Boolean
    open var needClientAuth: Boolean
    open var wantClientAuth: Boolean
    open val session: SSLSession
    open var enabledProtocols: Array<String>
    open val supportedCipherSuites: Array<String>
    open var enabledCipherSuites: Array<String>
}

expect abstract class SSLSocketFactory : SocketFactory {
    constructor()
    abstract fun getDefaultCipherSuites(): Array<String>
    abstract fun getSupportedCipherSuites(): Array<String>
    abstract fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket

    open val defaultCipherSuites: Array<String>
    open val supportedCipherSuites: Array<String>

    companion object {
        fun getDefault(): SSLSocketFactory
    }
}

expect interface HostnameVerifier {
    fun verify(hostname: String, session: SSLSession): Boolean
}

expect interface TrustManager
expect interface KeyManager

expect interface X509TrustManager : TrustManager {
    fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?)
    fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?)
    fun getAcceptedIssuers(): Array<X509Certificate>

    open val acceptedIssuers: Array<X509Certificate>
}

expect open class SSLContext {
    protected constructor()
    open fun init(km: Array<KeyManager>?, tm: Array<TrustManager>?, sr: Any?)
    open fun getSocketFactory(): SSLSocketFactory
    open fun getProtocol(): String

    open val socketFactory: SSLSocketFactory
    open val protocol: String

    companion object {
        fun getInstance(protocol: String): SSLContext
        fun getDefault(): SSLContext
    }
}

expect open class SSLException : IOException {
    constructor(reason: String?)
    constructor(reason: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect open class SSLHandshakeException : SSLException {
    constructor(reason: String?)
}

expect open class SSLPeerUnverifiedException : SSLException {
    constructor(reason: String?)
}

