@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.crypto

import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec

/**
 * [Nullability Decision] KeyGenerator.generateKey() returns non-null SecretKey.
 */
expect open class KeyGenerator {
    fun getAlgorithm(): String
    val algorithm: String
    fun init(random: SecureRandom)
    fun init(keysize: Int)
    fun init(keysize: Int, random: SecureRandom)
    fun init(params: AlgorithmParameterSpec)
    fun init(params: AlgorithmParameterSpec, random: SecureRandom)
    fun generateKey(): SecretKey

    companion object {
        fun getInstance(algorithm: String): KeyGenerator
    }
}
