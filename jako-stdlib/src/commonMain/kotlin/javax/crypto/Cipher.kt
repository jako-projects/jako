@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.crypto

import java.nio.ByteBuffer
import java.security.Key
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec

/**
 * [Nullability Decision] Cipher methods follow JDK 8 nullability contracts:
 * - getAlgorithm() returns non-null String.
 * - getIV() returns ByteArray? (null if cipher does not use an IV or before initialization).
 * - update(...) returns ByteArray? (null if input length is 0 in JDK 8, or empty/buffered).
 * - doFinal(...) returns non-null ByteArray.
 */
expect open class Cipher {
    fun getAlgorithm(): String
    val algorithm: String
    fun getBlockSize(): Int
    val blockSize: Int
    fun getOutputSize(inputLen: Int): Int
    fun getIV(): ByteArray?
    val iv: ByteArray?

    fun init(opmode: Int, key: Key)
    fun init(opmode: Int, key: Key, random: SecureRandom?)
    fun init(opmode: Int, key: Key, params: AlgorithmParameterSpec?)
    fun init(opmode: Int, key: Key, params: AlgorithmParameterSpec?, random: SecureRandom?)

    fun updateAAD(src: ByteArray)
    fun updateAAD(src: ByteArray, offset: Int, len: Int)
    fun updateAAD(src: ByteBuffer)

    fun update(input: ByteArray): ByteArray?
    fun update(input: ByteArray, inputOffset: Int, inputLen: Int): ByteArray?
    fun update(input: ByteArray, inputOffset: Int, inputLen: Int, output: ByteArray): Int
    fun update(input: ByteArray, inputOffset: Int, inputLen: Int, output: ByteArray, outputOffset: Int): Int

    fun doFinal(): ByteArray
    fun doFinal(output: ByteArray, outputOffset: Int): Int
    fun doFinal(input: ByteArray): ByteArray
    fun doFinal(input: ByteArray, inputOffset: Int, inputLen: Int): ByteArray
    fun doFinal(input: ByteArray, inputOffset: Int, inputLen: Int, output: ByteArray): Int
    fun doFinal(input: ByteArray, inputOffset: Int, inputLen: Int, output: ByteArray, outputOffset: Int): Int

    companion object {
        val ENCRYPT_MODE: Int
        val DECRYPT_MODE: Int
        val WRAP_MODE: Int
        val UNWRAP_MODE: Int
        val PUBLIC_KEY: Int
        val PRIVATE_KEY: Int
        val SECRET_KEY: Int

        fun getInstance(transformation: String): Cipher
    }
}
