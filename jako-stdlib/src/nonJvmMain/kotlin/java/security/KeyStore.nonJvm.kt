package java.security

import java.io.InputStream
import java.security.cert.Certificate
import java.util.Collections
import java.util.Enumeration

actual open class KeyStore protected actual constructor() {
    private var ksType: String = "PKCS12"
    private var initialized: Boolean = false
    internal val certificateEntries = LinkedHashMap<String, Certificate>()

    actual open fun load(stream: InputStream?, password: CharArray?) {
        certificateEntries.clear()
        initialized = true
    }

    actual open fun getType(): String = ksType
    actual open val type: String get() = ksType

    actual open fun setCertificateEntry(alias: String, cert: Certificate) {
        if (!initialized) throw KeyStoreException("KeyStore not initialized")
        certificateEntries[alias] = cert
    }

    actual open fun getCertificate(alias: String): Certificate? {
        if (!initialized) throw KeyStoreException("KeyStore not initialized")
        return certificateEntries[alias]
    }

    actual open fun getCertificateAlias(cert: Certificate): String? {
        if (!initialized) throw KeyStoreException("KeyStore not initialized")
        val targetBytes = cert.getEncoded()
        for ((alias, entryCert) in certificateEntries) {
            if (entryCert.getEncoded().contentEquals(targetBytes)) {
                return alias
            }
        }
        return null
    }

    actual open fun aliases(): Enumeration<String> {
        if (!initialized) throw KeyStoreException("KeyStore not initialized")
        return Collections.enumeration(certificateEntries.keys.toList())
    }

    actual open fun containsAlias(alias: String): Boolean {
        if (!initialized) throw KeyStoreException("KeyStore not initialized")
        return certificateEntries.containsKey(alias)
    }

    actual open fun deleteEntry(alias: String) {
        if (!initialized) throw KeyStoreException("KeyStore not initialized")
        certificateEntries.remove(alias)
    }

    actual open fun size(): Int {
        if (!initialized) throw KeyStoreException("KeyStore not initialized")
        return certificateEntries.size
    }

    actual open fun isCertificateEntry(alias: String): Boolean {
        if (!initialized) throw KeyStoreException("KeyStore not initialized")
        return certificateEntries.containsKey(alias)
    }

    actual companion object {
        actual fun getInstance(type: String): KeyStore {
            val ks = KeyStore()
            ks.ksType = type
            return ks
        }
        actual fun getDefaultType(): String = "PKCS12"
    }
}
