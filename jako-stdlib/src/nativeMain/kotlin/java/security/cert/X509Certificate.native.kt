package java.security.cert

import java.math.BigInteger
import java.security.*
import java.util.Date
import javax.security.auth.x500.X500Principal

actual abstract class X509Certificate : Certificate() {
    actual abstract fun checkValidity()
    actual abstract fun checkValidity(date: Date)
    actual abstract fun getVersion(): Int
    actual open val version: Int get() = getVersion()
    actual abstract fun getSerialNumber(): BigInteger
    actual open val serialNumber: BigInteger get() = getSerialNumber()
    actual open fun getSubjectDN(): Principal = getSubjectX500Principal()
    actual open val subjectDN: Principal get() = getSubjectDN()
    actual open fun getIssuerDN(): Principal = getIssuerX500Principal()
    actual open val issuerDN: Principal get() = getIssuerDN()
    actual open fun getSubjectX500Principal(): X500Principal = X500Principal("")
    actual open val subjectX500Principal: X500Principal get() = getSubjectX500Principal()
    actual open fun getIssuerX500Principal(): X500Principal = X500Principal("")
    actual open val issuerX500Principal: X500Principal get() = getIssuerX500Principal()
    actual abstract fun getNotBefore(): Date
    actual open val notBefore: Date get() = getNotBefore()
    actual abstract fun getNotAfter(): Date
    actual open val notAfter: Date get() = getNotAfter()
    actual abstract fun getSigAlgName(): String
    actual open val sigAlgName: String get() = getSigAlgName()
    actual abstract fun getBasicConstraints(): Int
    actual open val basicConstraints: Int get() = getBasicConstraints()
    actual open fun getSubjectAlternativeNames(): Collection<List<*>>? = null
    actual open val subjectAlternativeNames: Collection<List<*>>? get() = getSubjectAlternativeNames()
}
