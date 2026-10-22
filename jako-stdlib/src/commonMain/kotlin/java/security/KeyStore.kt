@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security

import java.io.InputStream
import java.security.cert.Certificate
import java.util.Enumeration

expect open class KeyStore {
    protected constructor()
    open fun load(stream: InputStream?, password: CharArray?)
    open fun getType(): String
    open val type: String
    open fun setCertificateEntry(alias: String, cert: Certificate)
    open fun getCertificate(alias: String): Certificate?
    open fun getCertificateAlias(cert: Certificate): String?
    open fun aliases(): Enumeration<String>
    open fun containsAlias(alias: String): Boolean
    open fun deleteEntry(alias: String)
    open fun size(): Int
    open fun isCertificateEntry(alias: String): Boolean

    companion object {
        fun getInstance(type: String): KeyStore
        fun getDefaultType(): String
    }
}
