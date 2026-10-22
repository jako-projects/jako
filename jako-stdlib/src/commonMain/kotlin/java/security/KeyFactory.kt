@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security

import java.security.spec.KeySpec

/**
 * [Nullability Decision] KeyFactory methods return non-null PublicKey, PrivateKey, KeySpec, and Key.
 */
expect open class KeyFactory {
    fun getAlgorithm(): String
    val algorithm: String
    fun generatePublic(keySpec: KeySpec): PublicKey
    fun generatePrivate(keySpec: KeySpec): PrivateKey
    fun <T : KeySpec> getKeySpec(key: Key, keySpec: Class<T>): T
    fun translateKey(key: Key): Key

    companion object {
        fun getInstance(algorithm: String): KeyFactory
    }
}
