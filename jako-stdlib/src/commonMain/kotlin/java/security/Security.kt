@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security

import java.nio.ByteBuffer
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.KeySpec
import java.util.Random

expect open class GeneralSecurityException : Exception {
    constructor()
    constructor(msg: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect open class NoSuchAlgorithmException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect open class DigestException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect open class KeyException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect open class InvalidKeyException : KeyException {
    constructor()
    constructor(msg: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect open class InvalidAlgorithmParameterException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect open class InvalidParameterException : IllegalArgumentException {
    constructor()
    constructor(msg: String?)
}

expect open class SignatureException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect abstract class MessageDigest {
    protected constructor(algorithm: String)

    open fun update(input: Byte)
    open fun update(input: ByteArray)
    abstract fun update(input: ByteArray, offset: Int, len: Int)
    open fun update(input: ByteBuffer)
    abstract fun digest(): ByteArray
    open fun digest(buf: ByteArray, offset: Int, len: Int): Int
    open fun digest(input: ByteArray): ByteArray
    abstract fun reset()
    fun getAlgorithm(): String
    val algorithm: String
    open fun getDigestLength(): Int
    val digestLength: Int

    companion object {
        fun getInstance(algorithm: String): MessageDigest
        fun isEqual(digesta: ByteArray?, digestb: ByteArray?): Boolean
    }
}

expect open class SecureRandom() : Random {
    override fun nextBytes(bytes: ByteArray)

    companion object {
        fun getInstance(algorithm: String): SecureRandom
    }
}

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

/**
 * [Nullability Decision] Signature methods follow JDK 8 nullability contracts:
 * - sign() returns non-null ByteArray.
 * - verify() takes non-null ByteArray and returns Boolean.
 */
expect abstract class Signature {
    fun getAlgorithm(): String
    val algorithm: String
    fun initVerify(publicKey: PublicKey)
    fun initSign(privateKey: PrivateKey)
    fun initSign(privateKey: PrivateKey, random: SecureRandom)
    fun sign(): ByteArray
    fun sign(outbuf: ByteArray, offset: Int, len: Int): Int
    fun verify(signature: ByteArray): Boolean
    fun verify(signature: ByteArray, offset: Int, length: Int): Boolean
    fun update(b: Byte)
    fun update(data: ByteArray)
    fun update(data: ByteArray, off: Int, len: Int)
    fun update(data: ByteBuffer)

    companion object {
        fun getInstance(algorithm: String): Signature
    }
}
