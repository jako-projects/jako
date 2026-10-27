package java.security.interfaces

import java.math.BigInteger

actual interface RSAKey {
    actual fun getModulus(): BigInteger
    actual val modulus: BigInteger get() = getModulus()
}
