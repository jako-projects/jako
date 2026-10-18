@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.crypto

import java.lang.Cloneable
import java.nio.ByteBuffer
import java.security.GeneralSecurityException
import java.security.Key
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.KeySpec

expect interface SecretKey : Key

expect open class ShortBufferException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
}

expect open class NoSuchPaddingException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
}

expect open class IllegalBlockSizeException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
}

expect open class BadPaddingException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
}

expect open class AEADBadTagException : BadPaddingException {
    constructor()
    constructor(msg: String?)
}

/**
 * [Nullability Decision] Mac methods follow JDK 8 nullability contracts:
 * - getAlgorithm() returns non-null String.
 * - update(ByteArray?) and doFinal(ByteArray?) accept nullable ByteArray (no-op on null in JDK 8).
 * - doFinal() returns non-null ByteArray.
 */
expect open class Mac : Cloneable {
    fun getAlgorithm(): String
    val algorithm: String
    fun getMacLength(): Int
    val macLength: Int
    fun init(key: Key)
    fun init(key: Key, params: AlgorithmParameterSpec?)
    fun update(input: Byte)
    fun update(input: ByteArray?)
    fun update(input: ByteArray?, offset: Int, len: Int)
    fun update(input: ByteBuffer)
    fun doFinal(): ByteArray
    fun doFinal(output: ByteArray, outOffset: Int)
    fun doFinal(input: ByteArray?): ByteArray
    fun reset()

    companion object {
        fun getInstance(algorithm: String): Mac
    }
}

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
