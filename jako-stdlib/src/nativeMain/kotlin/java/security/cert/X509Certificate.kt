package java.security.cert

import java.io.InputStream
import java.io.Serializable
import java.security.GeneralSecurityException
import java.security.Principal
import java.security.PublicKey
import javax.security.auth.x500.X500Principal

actual open class CertificateException : GeneralSecurityException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}

actual open class CertificateEncodingException : CertificateException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}

actual open class CertificateParsingException : CertificateException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}

actual abstract class Certificate : Serializable {
    open val type: String get() = "X.509"
    actual abstract fun getEncoded(): ByteArray
    /** [Nullability Decision] encoded is non-null, matching JDK 8 Certificate.getEncoded() */
    actual open val encoded: ByteArray get() = getEncoded()
    open val publicKey: PublicKey get() = object : PublicKey {
        override fun getAlgorithm(): String = ""
        override fun getFormat(): String? = null
        override fun getEncoded(): ByteArray? = null
    }
    open fun verify(key: PublicKey) {}
}

actual abstract class X509Certificate : Certificate() {
    abstract fun checkValidity()
    actual open fun getSubjectAlternativeNames(): Collection<List<*>>? = null
    actual open val subjectAlternativeNames: Collection<List<*>>? get() = getSubjectAlternativeNames()
    open val subjectX500Principal: X500Principal get() = X500Principal("")
    open val issuerX500Principal: X500Principal get() = X500Principal("")
    actual open fun getIssuerDN(): Any = object : Principal { override fun getName(): String = "" }
    actual open fun getSubjectDN(): Any = object : Principal { override fun getName(): String = "" }
    open val issuerDN: Principal get() = object : Principal { override fun getName(): String = "" }
    open val subjectDN: Principal get() = object : Principal { override fun getName(): String = "" }
    open val basicConstraints: Int get() = -1
}

actual open class CertificateFactory private constructor(val type: String) {
    actual open fun generateCertificate(inStream: InputStream): Certificate {
        val bytes = inStream.readBytes()
        return RawX509Certificate(bytes)
    }

    fun generateCertificate(bytes: ByteArray, dnsNames: List<String>): Certificate {
        return RawX509Certificate(bytes, dnsNames)
    }

    actual companion object {
        actual fun getInstance(type: String): CertificateFactory {
            return CertificateFactory(type)
        }
    }

    private class RawX509Certificate(
        private val rawData: ByteArray,
        private val dnsNames: List<String> = emptyList()
    ) : X509Certificate() {
        override fun checkValidity() {}
        override fun getEncoded(): ByteArray = rawData.copyOf()
        override fun getSubjectAlternativeNames(): Collection<List<*>>? {
            return if (dnsNames.isNotEmpty()) dnsNames.map { listOf(2, it) } else null
        }
    }
}
