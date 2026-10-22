package javax.crypto

import java.security.*
import java.security.spec.*
import javax.crypto.spec.SecretKeySpec

actual open class KeyGenerator private constructor(
    private val alg: String,
    private var keySizeBits: Int
) {
    private var random: SecureRandom = SecureRandom()

    actual fun getAlgorithm(): String = alg
    actual val algorithm: String get() = alg

    actual fun init(random: SecureRandom) {
        this.random = random
    }

    actual fun init(keysize: Int) {
        init(keysize, SecureRandom())
    }

    actual fun init(keysize: Int, random: SecureRandom) {
        if (alg.equals("AES", ignoreCase = true)) {
            if (keysize != 128 && keysize != 192 && keysize != 256) {
                throw InvalidParameterException("Wrong keysize: must be equal to 128, 192 or 256")
            }
        } else if (keysize <= 0) {
            throw InvalidParameterException("Key length must be positive")
        }
        this.keySizeBits = keysize
        this.random = random
    }

    actual fun init(params: AlgorithmParameterSpec) {
        throw InvalidAlgorithmParameterException("KeyGenerator.$alg does not support AlgorithmParameterSpec")
    }

    actual fun init(params: AlgorithmParameterSpec, random: SecureRandom) {
        throw InvalidAlgorithmParameterException("KeyGenerator.$alg does not support AlgorithmParameterSpec")
    }

    actual fun generateKey(): SecretKey {
        val numBytes = (keySizeBits + 7) / 8
        val bytes = ByteArray(numBytes)
        random.nextBytes(bytes)
        return SecretKeySpec(bytes, alg)
    }

    actual companion object {
        actual fun getInstance(algorithm: String): KeyGenerator {
            val normalized = algorithm.uppercase().replace("-", "")
            return when (normalized) {
                "AES" -> KeyGenerator(algorithm, 128)
                "HMACMD5" -> KeyGenerator(algorithm, 128)
                "HMACSHA1" -> KeyGenerator(algorithm, 160)
                "HMACSHA256" -> KeyGenerator(algorithm, 256)
                "HMACSHA384" -> KeyGenerator(algorithm, 384)
                "HMACSHA512" -> KeyGenerator(algorithm, 512)
                else -> throw NoSuchAlgorithmException("KeyGenerator.$algorithm not available")
            }
        }
    }
}
