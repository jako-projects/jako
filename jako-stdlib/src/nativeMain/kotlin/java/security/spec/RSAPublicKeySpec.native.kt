package java.security.spec

import java.math.BigInteger

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
