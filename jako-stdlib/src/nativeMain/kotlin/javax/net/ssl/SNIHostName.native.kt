package javax.net.ssl

import java.net.IDN
import java.security.cert.Certificate
import java.security.cert.X509Certificate

actual class SNIHostName {
    actual val asciiName: String

    actual constructor(hostname: String) {
        val converted = try {
            IDN.toASCII(hostname, IDN.USE_STD3_ASCII_RULES)
        } catch (e: RuntimeException) {
            throw IllegalArgumentException("Invalid hostname: $hostname", e)
        }
        validateHostName(converted)
        this.asciiName = converted
    }

    actual constructor(encoded: ByteArray) {
        if (encoded.isEmpty()) {
            throw IllegalArgumentException("Server name value of host_name cannot be empty")
        }
        for (b in encoded) {
            val v = b.toInt() and 0xFF
            if (v > 0x7F) {
                throw IllegalArgumentException("Server name value of host_name must be ASCII")
            }
        }
        val decoded = encoded.decodeToString()
        val converted = try {
            IDN.toASCII(decoded, IDN.USE_STD3_ASCII_RULES)
        } catch (e: RuntimeException) {
            throw IllegalArgumentException("Invalid encoded hostname", e)
        }
        validateHostName(converted)
        this.asciiName = converted
    }

    actual fun getAsciiName(): String = asciiName

    actual fun getEncoded(): ByteArray = asciiName.encodeToByteArray()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SNIHostName) return false
        return asciiName.equals(other.asciiName, ignoreCase = true)
    }

    override fun hashCode(): Int {
        var result = 17
        result = 31 * result + 0 // type = host_name (0)
        result = 31 * result + asciiName.uppercase().hashCode()
        return result
    }

    override fun toString(): String = "type=host_name (0), value=$asciiName"

    private fun validateHostName(name: String) {
        if (name.isEmpty()) {
            throw IllegalArgumentException("Server name value of host_name cannot be empty")
        }
        if (name.endsWith(".")) {
            throw IllegalArgumentException("Server name value of host_name cannot have a trailing dot")
        }
        val labels = name.split('.')
        for (label in labels) {
            if (label.isEmpty() || label.length > 63) {
                throw IllegalArgumentException("Invalid SNIHostName label: '$label' in '$name'")
            }
            if (label.startsWith('-') || label.endsWith('-')) {
                throw IllegalArgumentException("SNIHostName label cannot start or end with hyphen: '$label'")
            }
            for (ch in label) {
                val ok = ch in 'a'..'z' || ch in 'A'..'Z' || ch in '0'..'9' || ch == '-'
                if (!ok) {
                    throw IllegalArgumentException("Invalid character '$ch' in SNIHostName '$name'")
                }
            }
        }
    }
}

internal open class NativeSSLSession(
    private val host: String?,
    private val port: Int,
    private val peerCerts: Array<Certificate> = emptyArray(),
    private val negotiatedProtocol: String = "TLSv1.2",
    private val negotiatedCipherSuite: String = "TLS_AES_128_GCM_SHA256",
    private val sessionValid: Boolean = true,
    private val sniNames: List<Any>? = null
) : ExtendedSSLSession() {
    override fun getRequestedServerNames(): List<Any>? {
        if (sniNames != null) return sniNames
        val h = host ?: return emptyList()
        if (h.isEmpty() || h == "localhost" || isLiteralIpAddress(h)) {
            return emptyList()
        }
        return try {
            listOf(SNIHostName(h))
        } catch (_: IllegalArgumentException) {
            emptyList()
        }
    }
    override val requestedServerNames: List<Any>? get() = getRequestedServerNames()

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

private fun isLiteralIpAddress(host: String): Boolean {
    if (host.contains(':')) return true // IPv6
    val parts = host.split('.')
    if (parts.size == 4 && parts.all { it.isNotEmpty() && it.all { ch -> ch in '0'..'9' } }) {
        return true
    }
    return false
}
