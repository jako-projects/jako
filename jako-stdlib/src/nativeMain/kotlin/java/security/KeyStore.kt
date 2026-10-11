package java.security

import java.io.InputStream

actual open class KeyStore protected actual constructor() {
    private var ksType: String = "PKCS12"

    actual open fun load(stream: InputStream?, password: CharArray?) {}
    actual open fun getType(): String = ksType
    actual open val type: String get() = ksType

    actual companion object {
        actual fun getInstance(type: String): KeyStore {
            val ks = KeyStore()
            ks.ksType = type
            return ks
        }
        actual fun getDefaultType(): String = "PKCS12"
    }
}
