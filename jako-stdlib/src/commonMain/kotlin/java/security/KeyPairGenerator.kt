@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security

import java.security.spec.AlgorithmParameterSpec

/**
 * [Nullability Decision] KeyPairGenerator.genKeyPair() and generateKeyPair() return non-null KeyPair.
 */
expect abstract class KeyPairGenerator {
    open fun getAlgorithm(): String
    open val algorithm: String
    open fun initialize(keysize: Int)
    open fun initialize(keysize: Int, random: SecureRandom)
    open fun initialize(params: AlgorithmParameterSpec)
    open fun initialize(params: AlgorithmParameterSpec, random: SecureRandom)
    fun genKeyPair(): KeyPair
    open fun generateKeyPair(): KeyPair

    companion object {
        fun getInstance(algorithm: String): KeyPairGenerator
    }
}
