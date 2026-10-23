@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.interfaces

import java.math.BigInteger

expect interface RSAKey {
    fun getModulus(): BigInteger
    open val modulus: BigInteger
}
