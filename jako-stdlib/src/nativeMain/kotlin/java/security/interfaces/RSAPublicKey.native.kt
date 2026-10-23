package java.security.interfaces

import java.math.BigInteger
import java.security.PublicKey

actual interface RSAPublicKey : PublicKey, RSAKey {
    actual fun getPublicExponent(): BigInteger
    actual val publicExponent: BigInteger get() = getPublicExponent()
}
