@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.crypto

import java.security.spec.KeySpec

/**
 * [Nullability Decision] SecretKeyFactory.generateSecret() and translateKey() return non-null SecretKey.
 */
expect open class SecretKeyFactory {
    fun getAlgorithm(): String
    val algorithm: String
    fun generateSecret(keySpec: KeySpec): SecretKey
    fun translateKey(key: SecretKey): SecretKey

    companion object {
        fun getInstance(algorithm: String): SecretKeyFactory
    }
}
