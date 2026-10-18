@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.spec

import java.math.BigInteger
import java.security.GeneralSecurityException

expect interface KeySpec

expect interface AlgorithmParameterSpec

expect open class InvalidKeySpecException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect open class InvalidParameterSpecException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
}

/**
 * [Nullability Decision] EncodedKeySpec.getEncoded() returns non-null cloned ByteArray.
 */
expect abstract class EncodedKeySpec(encodedKey: ByteArray) : KeySpec {
    open fun getEncoded(): ByteArray
    open val encoded: ByteArray
    abstract fun getFormat(): String
    open val format: String
}

expect open class X509EncodedKeySpec(encodedKey: ByteArray) : EncodedKeySpec {
    override fun getEncoded(): ByteArray
    override val encoded: ByteArray
    override fun getFormat(): String
    override val format: String
}

expect open class PKCS8EncodedKeySpec(encodedKey: ByteArray) : EncodedKeySpec {
    override fun getEncoded(): ByteArray
    override val encoded: ByteArray
    override fun getFormat(): String
    override val format: String
}

expect open class RSAPublicKeySpec(modulus: BigInteger, publicExponent: BigInteger) : KeySpec {
    open fun getModulus(): BigInteger
    open val modulus: BigInteger
    open fun getPublicExponent(): BigInteger
    open val publicExponent: BigInteger
}

expect open class RSAPrivateKeySpec(modulus: BigInteger, privateExponent: BigInteger) : KeySpec {
    open fun getModulus(): BigInteger
    open val modulus: BigInteger
    open fun getPrivateExponent(): BigInteger
    open val privateExponent: BigInteger
}

expect open class ECGenParameterSpec(stdName: String) : AlgorithmParameterSpec {
    open fun getName(): String
    open val name: String
}
