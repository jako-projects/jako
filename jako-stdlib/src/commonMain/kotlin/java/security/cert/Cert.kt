@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.cert

import java.io.InputStream
import java.io.Serializable
import java.security.GeneralSecurityException

expect open class CertificateException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect open class CertificateEncodingException : CertificateException {
    constructor()
    constructor(message: String?)
}

expect open class CertificateParsingException : CertificateException {
    constructor()
    constructor(message: String?)
}

expect abstract class Certificate : Serializable {
    abstract fun getEncoded(): ByteArray
    open val encoded: ByteArray
}

expect abstract class X509Certificate : Certificate {
    open fun getSubjectDN(): Any
    open fun getIssuerDN(): Any
    open fun getSubjectAlternativeNames(): Collection<List<*>>?
    open val subjectAlternativeNames: Collection<List<*>>?
}

expect open class CertificateFactory {
    open fun generateCertificate(inStream: InputStream): Certificate

    companion object {
        fun getInstance(type: String): CertificateFactory
    }
}