package javax.net.ssl

import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

internal class SystemX509TrustManager : X509TrustManager {
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        if (chain.isNullOrEmpty()) throw IllegalArgumentException("Empty client certificate chain")
        for (cert in chain) cert.checkValidity()
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        if (chain.isNullOrEmpty()) throw IllegalArgumentException("Empty server certificate chain")
        for (cert in chain) cert.checkValidity()
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
}

internal class KeyStoreX509TrustManager(
    private val trustedCerts: List<X509Certificate>
) : X509TrustManager {

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        verifyChain(chain)
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        verifyChain(chain)
    }

    private fun verifyChain(chain: Array<out X509Certificate>?) {
        if (chain.isNullOrEmpty()) throw IllegalArgumentException("Certificate chain is null or empty")
        if (trustedCerts.isEmpty()) {
            throw CertificateException("No trusted certificates in KeyStore")
        }
        for (cert in chain) {
            cert.checkValidity()
        }
        for (cert in chain) {
            val certEncoded = cert.getEncoded()
            for (trusted in trustedCerts) {
                if (certEncoded.contentEquals(trusted.getEncoded())) {
                    return
                }
                if (cert.getIssuerX500Principal() == trusted.getSubjectX500Principal()) {
                    try {
                        cert.verify(trusted.getPublicKey())
                        return
                    } catch (_: Exception) {
                    }
                }
            }
        }
        throw CertificateException("Untrusted certificate chain: subject ${chain[0].getSubjectX500Principal().name}")
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = trustedCerts.toTypedArray()
}

actual open class TrustManagerFactory protected constructor() {
    private var currentTrustManager: X509TrustManager = SystemX509TrustManager()

    actual open fun init(ks: KeyStore?) {
        currentTrustManager = if (ks == null) {
            SystemX509TrustManager()
        } else {
            val certs = ks.certificateEntries.values.filterIsInstance<X509Certificate>()
            KeyStoreX509TrustManager(certs)
        }
    }

    actual open fun getTrustManagers(): Array<TrustManager>? = arrayOf(currentTrustManager)
    actual open val trustManagers: Array<TrustManager>? get() = getTrustManagers()

    actual companion object {
        actual fun getDefaultAlgorithm(): String = "PKIX"
        actual fun getInstance(algorithm: String): TrustManagerFactory = TrustManagerFactory()
    }
}
