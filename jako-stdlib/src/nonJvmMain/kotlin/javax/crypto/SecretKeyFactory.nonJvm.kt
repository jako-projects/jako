package javax.crypto

import java.security.*
import java.security.spec.*
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

actual open class SecretKeyFactory private constructor(
    private val alg: String,
    private val prfAlg: String?
) {
    actual fun getAlgorithm(): String = alg
    actual val algorithm: String get() = alg

    actual fun generateSecret(keySpec: KeySpec): SecretKey {
        if (prfAlg == null) {
            if (keySpec is SecretKeySpec) {
                return SecretKeySpec(keySpec.getEncoded(), alg)
            }
            throw InvalidKeySpecException("Unsupported KeySpec for $alg")
        }
        if (keySpec !is PBEKeySpec) {
            throw InvalidKeySpecException("PBEKeySpec required for PBKDF2")
        }
        val salt = keySpec.getSalt() ?: throw InvalidKeySpecException("Salt not found")
        val iterations = keySpec.getIterationCount()
        val keyLenBits = keySpec.getKeyLength()
        if (iterations <= 0 || keyLenBits <= 0) {
            throw InvalidKeySpecException("Iteration count and key length must be positive")
        }
        val passwordBytes = keySpec.getPassword().concatToString().encodeToByteArray()
        val mac = Mac.getInstance(prfAlg)
        // SecretKeySpec rejects empty byte arrays, so handle empty password by feeding single zero byte if empty
        val macKeyBytes = if (passwordBytes.isEmpty()) ByteArray(1) else passwordBytes
        if (passwordBytes.isEmpty()) {
            // Initialize HMAC with 0-length key directly using internal Mac logic:
            mac.init(SecretKeySpec(ByteArray(mac.getMacLength()), prfAlg))
        } else {
            mac.init(SecretKeySpec(macKeyBytes, prfAlg))
        }
        val hLen = mac.getMacLength()
        val dkLen = (keyLenBits + 7) / 8
        val numBlocks = (dkLen + hLen - 1) / hLen
        val derived = ByteArray(dkLen)
        val blockInput = ByteArray(salt.size + 4)
        salt.copyInto(blockInput, 0)

        var outOffset = 0
        for (blockIdx in 1..numBlocks) {
            blockInput[salt.size] = ((blockIdx ushr 24) and 0xFF).toByte()
            blockInput[salt.size + 1] = ((blockIdx ushr 16) and 0xFF).toByte()
            blockInput[salt.size + 2] = ((blockIdx ushr 8) and 0xFF).toByte()
            blockInput[salt.size + 3] = (blockIdx and 0xFF).toByte()

            var u = mac.doFinal(blockInput)
            val t = u.copyOf()
            for (iter in 2..iterations) {
                u = mac.doFinal(u)
                for (j in t.indices) {
                    t[j] = (t[j].toInt() xor u[j].toInt()).toByte()
                }
            }
            val copyLen = minOf(hLen, dkLen - outOffset)
            t.copyInto(derived, outOffset, 0, copyLen)
            outOffset += copyLen
        }
        return SecretKeySpec(derived, alg)
    }

    actual fun translateKey(key: SecretKey): SecretKey {
        val encoded = key.getEncoded() ?: throw InvalidKeyException("Key encoding is null")
        return SecretKeySpec(encoded, alg)
    }

    actual companion object {
        actual fun getInstance(algorithm: String): SecretKeyFactory {
            val upper = algorithm.uppercase().replace("-", "")
            return when (upper) {
                "PBKDF2WITHHMACSHA1" -> SecretKeyFactory(algorithm, "HmacSHA1")
                "PBKDF2WITHHMACSHA256" -> SecretKeyFactory(algorithm, "HmacSHA256")
                "PBKDF2WITHHMACSHA384" -> SecretKeyFactory(algorithm, "HmacSHA384")
                "PBKDF2WITHHMACSHA512" -> SecretKeyFactory(algorithm, "HmacSHA512")
                "AES" -> SecretKeyFactory(algorithm, null)
                else -> throw NoSuchAlgorithmException("SecretKeyFactory.$algorithm not available")
            }
        }
    }
}
