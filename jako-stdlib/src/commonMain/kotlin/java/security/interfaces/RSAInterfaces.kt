@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.interfaces

import java.math.BigInteger
import java.security.PrivateKey
import java.security.PublicKey

expect interface RSAKey {
    fun getModulus(): BigInteger
    open val modulus: BigInteger
}

expect interface RSAPublicKey : PublicKey, RSAKey {
    fun getPublicExponent(): BigInteger
    open val publicExponent: BigInteger
}

expect interface RSAPrivateKey : PrivateKey, RSAKey {
    fun getPrivateExponent(): BigInteger
    open val privateExponent: BigInteger
}
