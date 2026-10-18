package java.security.interfaces

import java.math.BigInteger
import java.security.PrivateKey
import java.security.PublicKey

actual interface RSAKey {
    actual fun getModulus(): BigInteger
    actual val modulus: BigInteger get() = getModulus()
}

actual interface RSAPublicKey : PublicKey, RSAKey {
    actual fun getPublicExponent(): BigInteger
    actual val publicExponent: BigInteger get() = getPublicExponent()
}

actual interface RSAPrivateKey : PrivateKey, RSAKey {
    actual fun getPrivateExponent(): BigInteger
    actual val privateExponent: BigInteger get() = getPrivateExponent()
}
