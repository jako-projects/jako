@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.cert

import java.io.InputStream

expect open class CertificateFactory {
    open fun generateCertificate(inStream: InputStream): Certificate
    open fun generateCertificates(inStream: InputStream): Collection<Certificate>

    companion object {
        fun getInstance(type: String): CertificateFactory
    }
}
