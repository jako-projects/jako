package javax.net.ssl

import java.security.cert.Certificate
import java.security.cert.X509Certificate

actual class SNIHostName actual constructor(actual val asciiName: String) {
    actual fun getAsciiName(): String = asciiName
}

internal open class NativeSSLSession(
    private val host: String?,
    private val port: Int,
    private val peerCerts: Array<Certificate> = emptyArray(),
    private val negotiatedProtocol: String = "TLSv1.2",
    private val negotiatedCipherSuite: String = "TLS_AES_128_GCM_SHA256",
    private val sessionValid: Boolean = true
) : SSLSession {
    override fun getPeerHost(): String? = host
    override val peerHost: String? get() = getPeerHost()
    override fun getPeerPort(): Int = port
    override val peerPort: Int get() = getPeerPort()
    override fun getPeerPrincipal(): Any? {
        val cert = peerCerts.firstOrNull() as? X509Certificate
            ?: throw SSLPeerUnverifiedException("peer not authenticated")
        return cert.getSubjectX500Principal()
    }
    override val peerPrincipal: Any? get() = getPeerPrincipal()
    override fun isValid(): Boolean = sessionValid
    override val isValid: Boolean get() = isValid()
    override fun getCipherSuite(): String = negotiatedCipherSuite
    override val cipherSuite: String get() = getCipherSuite()
    override fun getProtocol(): String = negotiatedProtocol
    override val protocol: String get() = getProtocol()
    override fun getPeerCertificates(): Array<Certificate> {
        if (peerCerts.isEmpty()) throw SSLPeerUnverifiedException("peer not authenticated")
        return peerCerts.copyOf()
    }
    override val peerCertificates: Array<Certificate> get() = getPeerCertificates()
}
