package java.security.spec

import java.math.BigInteger
import java.security.GeneralSecurityException

actual interface KeySpec

actual interface AlgorithmParameterSpec

actual open class InvalidKeySpecException : GeneralSecurityException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}

actual open class InvalidParameterSpecException : GeneralSecurityException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}

actual abstract class EncodedKeySpec actual constructor(encodedKey: ByteArray) : KeySpec {
    private val keyBytes: ByteArray = encodedKey.copyOf()

    actual open fun getEncoded(): ByteArray = keyBytes.copyOf()
    actual open val encoded: ByteArray get() = getEncoded()
    actual abstract fun getFormat(): String
    actual open val format: String get() = getFormat()
}

actual open class X509EncodedKeySpec actual constructor(encodedKey: ByteArray) : EncodedKeySpec(encodedKey) {
    actual override fun getEncoded(): ByteArray = super.getEncoded()
    actual override val encoded: ByteArray get() = getEncoded()
    actual override fun getFormat(): String = "X.509"
    actual override val format: String get() = "X.509"
}

actual open class PKCS8EncodedKeySpec actual constructor(encodedKey: ByteArray) : EncodedKeySpec(encodedKey) {
    actual override fun getEncoded(): ByteArray = super.getEncoded()
    actual override val encoded: ByteArray get() = getEncoded()
    actual override fun getFormat(): String = "PKCS#8"
    actual override val format: String get() = "PKCS#8"
}

actual open class RSAPublicKeySpec actual constructor(
    modulus: BigInteger,
    publicExponent: BigInteger
) : KeySpec {
    private val mod: BigInteger = modulus
    private val pubExp: BigInteger = publicExponent
    actual open fun getModulus(): BigInteger = mod
    actual open val modulus: BigInteger get() = mod
    actual open fun getPublicExponent(): BigInteger = pubExp
    actual open val publicExponent: BigInteger get() = pubExp
}

actual open class RSAPrivateKeySpec actual constructor(
    modulus: BigInteger,
    privateExponent: BigInteger
) : KeySpec {
    private val mod: BigInteger = modulus
    private val privExp: BigInteger = privateExponent
    actual open fun getModulus(): BigInteger = mod
    actual open val modulus: BigInteger get() = mod
    actual open fun getPrivateExponent(): BigInteger = privExp
    actual open val privateExponent: BigInteger get() = privExp
}

actual open class ECGenParameterSpec actual constructor(
    private val stdName: String
) : AlgorithmParameterSpec {
    actual open fun getName(): String = stdName
    actual open val name: String get() = stdName
}
