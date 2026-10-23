@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.interfaces

import java.math.BigInteger
import java.security.PublicKey

expect interface RSAPublicKey : PublicKey, RSAKey {
    fun getPublicExponent(): BigInteger
    open val publicExponent: BigInteger
}
