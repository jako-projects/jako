@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.interfaces

import java.math.BigInteger
import java.security.PrivateKey

expect interface RSAPrivateKey : PrivateKey, RSAKey {
    fun getPrivateExponent(): BigInteger
    open val privateExponent: BigInteger
}
