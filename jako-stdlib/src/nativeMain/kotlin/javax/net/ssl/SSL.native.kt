package javax.net.ssl

import java.net.Socket
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.X509Certificate

actual interface KeyManager

actual interface TrustManager

actual interface X509TrustManager : TrustManager {
    actual fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?)
    actual fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?)
    actual fun getAcceptedIssuers(): Array<X509Certificate>
    actual open val acceptedIssuers: Array<X509Certificate> get() = getAcceptedIssuers()
}

actual interface HostnameVerifier {
    actual fun verify(hostname: String, session: SSLSession): Boolean
}

actual interface SSLSession {
    fun getPeerHost(): String?
    fun getPeerPort(): Int
    actual fun getPeerPrincipal(): Any? = null
    actual open val peerPrincipal: Any? get() = getPeerPrincipal()
    actual fun isValid(): Boolean
    actual open val isValid: Boolean get() = isValid()
    actual fun getCipherSuite(): String
    actual open val cipherSuite: String get() = getCipherSuite()
    actual fun getProtocol(): String
    actual open val protocol: String get() = getProtocol()
    actual fun getPeerCertificates(): Array<Certificate>
    actual open val peerCertificates: Array<Certificate> get() = getPeerCertificates()
    fun getLocalCertificates(): Array<Certificate>? = null
    val localCertificates: Array<Certificate>? get() = getLocalCertificates()
}

actual abstract class ExtendedSSLSession actual constructor() : SSLSession {
    actual open fun getRequestedServerNames(): List<Any>? = null
    actual open val requestedServerNames: List<Any>? get() = getRequestedServerNames()
}

actual class SNIHostName actual constructor(actual val asciiName: String) {
    actual fun getAsciiName(): String = asciiName
}

internal open class NativeSSLSession(
    private val host: String?,
    private val port: Int,
    private val peerCerts: Array<Certificate> = emptyArray()
) : SSLSession {
    override fun getPeerHost(): String? = host
    override fun getPeerPort(): Int = port
    override val peerPrincipal: Any? get() = getPeerPrincipal()
    override fun isValid(): Boolean = true
    override val isValid: Boolean get() = isValid()
    override fun getCipherSuite(): String = "TLS_AES_128_GCM_SHA256"
    override val cipherSuite: String get() = getCipherSuite()
    override fun getProtocol(): String = "TLSv1.3"
    override val protocol: String get() = getProtocol()
    override fun getPeerCertificates(): Array<Certificate> {
        if (peerCerts.isEmpty()) throw SSLPeerUnverifiedException("peer not authenticated")
        return peerCerts
    }
    override val peerCertificates: Array<Certificate> get() = getPeerCertificates()
}

actual abstract class SSLSocketFactory actual constructor() : javax.net.SocketFactory() {
    actual abstract fun getDefaultCipherSuites(): Array<String>
    actual abstract fun getSupportedCipherSuites(): Array<String>
    actual abstract fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket

    actual open val defaultCipherSuites: Array<String> get() = getDefaultCipherSuites()
    actual open val supportedCipherSuites: Array<String> get() = getSupportedCipherSuites()

    actual companion object {
        actual fun getDefault(): SSLSocketFactory = NativeSSLSocketFactory()
    }
}

internal expect fun createPlatformSSLSocket(s: Socket, host: String, port: Int, autoClose: Boolean): SSLSocket

internal class NativeSSLSocketFactory : SSLSocketFactory() {
    override fun getDefaultCipherSuites(): Array<String> = arrayOf("TLS_AES_128_GCM_SHA256")
    override fun getSupportedCipherSuites(): Array<String> = arrayOf("TLS_AES_128_GCM_SHA256")

    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return createPlatformSSLSocket(s, host, port, autoClose)
    }

    override fun createSocket(host: String, port: Int): Socket {
        val s = Socket(host, port)
        return createPlatformSSLSocket(s, host, port, true)
    }

    override fun createSocket(host: String, port: Int, localHost: java.net.InetAddress, localPort: Int): Socket {
        return createSocket(host, port)
    }

    override fun createSocket(host: java.net.InetAddress, port: Int): Socket {
        return createSocket(host.getHostAddress(), port)
    }

    override fun createSocket(address: java.net.InetAddress, port: Int, localAddress: java.net.InetAddress, localPort: Int): Socket {
        return createSocket(address.getHostAddress(), port)
    }
}

actual abstract class SSLSocket actual protected constructor() : Socket() {
    actual abstract fun startHandshake()
    actual abstract fun getSession(): SSLSession
    actual open val session: SSLSession get() = getSession()
    actual abstract fun setUseClientMode(mode: Boolean)
    actual abstract fun getUseClientMode(): Boolean
    actual open fun setNeedClientAuth(need: Boolean) {}
    actual open fun getNeedClientAuth(): Boolean = false
    actual open fun setWantClientAuth(want: Boolean) {}
    actual open fun getWantClientAuth(): Boolean = false
    actual abstract fun setEnabledProtocols(protocols: Array<String>?)
    actual abstract fun getEnabledProtocols(): Array<String>
    actual open var enabledProtocols: Array<String>
        get() = getEnabledProtocols()
        set(value) = setEnabledProtocols(value)

    actual abstract fun getSupportedCipherSuites(): Array<String>
    actual abstract fun setEnabledCipherSuites(suites: Array<String>?)
    actual abstract fun getEnabledCipherSuites(): Array<String>
    actual open var enabledCipherSuites: Array<String>
        get() = getEnabledCipherSuites()
        set(value) = setEnabledCipherSuites(value)

    actual open var useClientMode: Boolean
        get() = getUseClientMode()
        set(value) = setUseClientMode(value)

    actual open var needClientAuth: Boolean
        get() = getNeedClientAuth()
        set(value) = setNeedClientAuth(value)

    actual open var wantClientAuth: Boolean
        get() = getWantClientAuth()
        set(value) = setWantClientAuth(value)

    actual open val supportedCipherSuites: Array<String>
        get() = getSupportedCipherSuites()
}

internal class NativeSSLSocket(
    private val host: String,
    private val peerPort: Int
) : SSLSocket() {
    private var clientMode = true
    private var needAuth = false
    private var wantAuth = false
    private var protocols = arrayOf("TLSv1.2", "TLSv1.3")
    private var cipherSuites = arrayOf(
        "TLS_AES_128_GCM_SHA256",
        "TLS_AES_256_GCM_SHA384",
        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
    )
    private val currentSession = NativeSSLSession(host, peerPort)

    override fun startHandshake() {
    }

    override fun getSession(): SSLSession = currentSession

    override fun setUseClientMode(mode: Boolean) {
        clientMode = mode
    }

    override fun getUseClientMode(): Boolean = clientMode

    override fun setNeedClientAuth(need: Boolean) {
        needAuth = need
    }

    override fun setWantClientAuth(want: Boolean) {
        wantAuth = want
    }

    override fun setEnabledProtocols(protocols: Array<String>?) {
        if (protocols != null) {
            this.protocols = protocols.copyOf()
        }
    }

    override fun getEnabledProtocols(): Array<String> = protocols.copyOf()

    override fun getSupportedCipherSuites(): Array<String> = cipherSuites.copyOf()

    override fun setEnabledCipherSuites(suites: Array<String>?) {
        if (suites != null) {
            this.cipherSuites = suites.copyOf()
        }
    }

    override fun getEnabledCipherSuites(): Array<String> = cipherSuites.copyOf()
}

actual open class SSLContext actual protected constructor() {
    actual open fun init(km: Array<KeyManager>?, tm: Array<TrustManager>?, sr: Any?) {
    }

    actual open fun getSocketFactory(): SSLSocketFactory = NativeSSLSocketFactory()
    actual open val socketFactory: SSLSocketFactory get() = getSocketFactory()

    actual open fun getProtocol(): String = "TLS"
    actual open val protocol: String get() = getProtocol()

    actual companion object {
        actual fun getInstance(protocol: String): SSLContext = SSLContext()
        actual fun getDefault(): SSLContext = SSLContext()
    }
}

actual open class SSLException : java.io.IOException {
    actual constructor(reason: String?) : super(reason)
    actual constructor(reason: String?, cause: Throwable?) : super(reason, cause)
    actual constructor(cause: Throwable?) : super(cause)
}

actual open class SSLHandshakeException actual constructor(reason: String?) : SSLException(reason)
actual open class SSLPeerUnverifiedException actual constructor(reason: String?) : SSLException(reason)
