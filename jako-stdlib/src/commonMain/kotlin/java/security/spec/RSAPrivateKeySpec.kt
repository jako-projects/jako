@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.spec

import java.math.BigInteger

expect open class RSAPrivateKeySpec(modulus: BigInteger, privateExponent: BigInteger) : KeySpec {
    open fun getModulus(): BigInteger
    open val modulus: BigInteger
    open fun getPrivateExponent(): BigInteger
    open val privateExponent: BigInteger
}
