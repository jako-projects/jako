package java.security.interfaces

import java.math.BigInteger
import java.security.PrivateKey

actual interface RSAPrivateKey : PrivateKey, RSAKey {
    actual fun getPrivateExponent(): BigInteger
    actual val privateExponent: BigInteger get() = getPrivateExponent()
}
