package java.security.cert

import java.io.Serializable
import java.security.*

actual abstract class Certificate : Serializable {
    actual open fun getType(): String = "X.509"
    actual open val type: String get() = getType()
    actual abstract fun getEncoded(): ByteArray
    /** [Nullability Decision] encoded is non-null, matching JDK 8 Certificate.getEncoded() */
    actual open val encoded: ByteArray get() = getEncoded()
    actual abstract fun getPublicKey(): PublicKey
    actual open val publicKey: PublicKey get() = getPublicKey()
    actual abstract fun verify(key: PublicKey)
}
