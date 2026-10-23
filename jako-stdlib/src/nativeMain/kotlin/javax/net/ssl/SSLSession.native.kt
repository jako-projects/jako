package javax.net.ssl

import java.security.cert.Certificate

actual interface SSLSession {
    actual fun getPeerHost(): String?
    actual open val peerHost: String? get() = getPeerHost()
    actual fun getPeerPort(): Int
    actual open val peerPort: Int get() = getPeerPort()
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
