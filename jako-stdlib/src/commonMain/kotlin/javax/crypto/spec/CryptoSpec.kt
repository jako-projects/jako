@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.crypto.spec

import java.security.spec.AlgorithmParameterSpec
import java.security.spec.KeySpec
import javax.crypto.SecretKey

/**
 * [Nullability Decision] SecretKeySpec methods return non-null String ("RAW" for format) and non-null ByteArray.
 */
expect open class SecretKeySpec : KeySpec, SecretKey {
    constructor(key: ByteArray, algorithm: String)
    constructor(key: ByteArray, offset: Int, len: Int, algorithm: String)

    override fun getAlgorithm(): String
    override val algorithm: String
    override fun getFormat(): String
    override val format: String
    override fun getEncoded(): ByteArray
    override val encoded: ByteArray
}

/**
 * [Nullability Decision] IvParameterSpec.getIV() returns non-null ByteArray (cloned copy).
 */
expect open class IvParameterSpec : AlgorithmParameterSpec {
    constructor(iv: ByteArray)
    constructor(iv: ByteArray, offset: Int, len: Int)

    fun getIV(): ByteArray
    val iv: ByteArray
}

/**
 * [Nullability Decision] GCMParameterSpec.getIV() returns non-null cloned ByteArray; tLen is tag length in bits.
 */
expect open class GCMParameterSpec : AlgorithmParameterSpec {
    constructor(tLen: Int, src: ByteArray)
    constructor(tLen: Int, src: ByteArray, offset: Int, len: Int)

    fun getTLen(): Int
    val tLen: Int
    fun getIV(): ByteArray
    val iv: ByteArray
}

/**
 * [Nullability Decision] PBEKeySpec accepts nullable CharArray password (treated as empty char array in JDK 8)
 * and returns nullable ByteArray? from getSalt() when constructed with password-only constructor.
 */
expect open class PBEKeySpec : KeySpec {
    constructor(password: CharArray?)
    constructor(password: CharArray?, salt: ByteArray, iterationCount: Int, keyLength: Int)
    constructor(password: CharArray?, salt: ByteArray, iterationCount: Int)

    fun clearPassword()
    fun getPassword(): CharArray
    val password: CharArray
    fun getSalt(): ByteArray?
    val salt: ByteArray?
    fun getIterationCount(): Int
    val iterationCount: Int
    fun getKeyLength(): Int
    val keyLength: Int
}
