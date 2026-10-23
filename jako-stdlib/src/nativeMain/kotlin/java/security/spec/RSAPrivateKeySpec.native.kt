package java.security.spec

import java.math.BigInteger

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
