@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.cert

import java.math.BigInteger
import java.security.Principal
import java.util.Date
import javax.security.auth.x500.X500Principal

expect abstract class X509Certificate : Certificate {
    abstract fun checkValidity()
    abstract fun checkValidity(date: Date)
    abstract fun getVersion(): Int
    open val version: Int
    abstract fun getSerialNumber(): BigInteger
    open val serialNumber: BigInteger
    open fun getSubjectDN(): Principal
    open val subjectDN: Principal
    open fun getIssuerDN(): Principal
    open val issuerDN: Principal
    open fun getSubjectX500Principal(): X500Principal
    open val subjectX500Principal: X500Principal
    open fun getIssuerX500Principal(): X500Principal
    open val issuerX500Principal: X500Principal
    abstract fun getNotBefore(): Date
    open val notBefore: Date
    abstract fun getNotAfter(): Date
    open val notAfter: Date
    abstract fun getSigAlgName(): String
    open val sigAlgName: String
    abstract fun getBasicConstraints(): Int
    open val basicConstraints: Int
    open fun getSubjectAlternativeNames(): Collection<List<*>>?
    open val subjectAlternativeNames: Collection<List<*>>?
}
