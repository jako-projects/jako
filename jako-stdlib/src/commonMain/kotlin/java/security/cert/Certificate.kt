@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.cert

import java.io.Serializable
import java.security.PublicKey

expect abstract class Certificate : Serializable {
    open fun getType(): String
    open val type: String
    abstract fun getEncoded(): ByteArray
    open val encoded: ByteArray
    abstract fun getPublicKey(): PublicKey
    open val publicKey: PublicKey
    abstract fun verify(key: PublicKey)
}
