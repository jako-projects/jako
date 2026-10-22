package java.security

import java.io.Serializable

actual class KeyPair actual constructor(
    private val publicKey: PublicKey?,
    private val privateKey: PrivateKey?
) : Serializable {
    actual fun getPublic(): PublicKey? = publicKey
    actual val `public`: PublicKey? get() = publicKey
    actual fun getPrivate(): PrivateKey? = privateKey
    actual val `private`: PrivateKey? get() = privateKey
}
