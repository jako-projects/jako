@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.spec

import java.math.BigInteger

expect open class RSAPublicKeySpec(modulus: BigInteger, publicExponent: BigInteger) : KeySpec {
    open fun getModulus(): BigInteger
    open val modulus: BigInteger
    open fun getPublicExponent(): BigInteger
    open val publicExponent: BigInteger
}
