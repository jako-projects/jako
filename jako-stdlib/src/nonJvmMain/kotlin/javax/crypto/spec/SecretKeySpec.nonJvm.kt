package javax.crypto.spec

import java.security.MessageDigest
import java.security.spec.KeySpec
import javax.crypto.SecretKey

actual open class SecretKeySpec : KeySpec, SecretKey {
    private val keyBytes: ByteArray
    private val alg: String

    actual constructor(key: ByteArray, algorithm: String) {
        if (key.isEmpty()) {
            throw IllegalArgumentException("Empty key")
        }
        if (algorithm.isEmpty()) {
            throw IllegalArgumentException("Missing algorithm")
        }
        this.keyBytes = key.copyOf()
        this.alg = algorithm
    }

    actual constructor(key: ByteArray, offset: Int, len: Int, algorithm: String) {
        if (key.isEmpty()) {
            throw IllegalArgumentException("Empty key")
        }
        if (offset < 0) {
            throw IndexOutOfBoundsException("offset is negative")
        }
        if (len < 0) {
            throw IndexOutOfBoundsException("len is negative")
        }
        if (key.size - offset < len) {
            throw IllegalArgumentException("Invalid offset/length combination")
        }
        if (len == 0) {
            throw IllegalArgumentException("Empty key")
        }
        if (algorithm.isEmpty()) {
            throw IllegalArgumentException("Missing algorithm")
        }
        this.keyBytes = key.copyOfRange(offset, offset + len)
        this.alg = algorithm
    }

    actual override fun getAlgorithm(): String = alg
    actual override val algorithm: String get() = alg

    actual override fun getFormat(): String = "RAW"
    actual override val format: String get() = "RAW"

    actual override fun getEncoded(): ByteArray = keyBytes.copyOf()
    actual override val encoded: ByteArray get() = getEncoded()

    override fun hashCode(): Int {
        var retval = 0
        for (i in 1 until keyBytes.size) {
            retval += keyBytes[i].toInt() * i
        }
        return if (alg.equals("TripleDES", ignoreCase = true)) {
            retval xor "desede".hashCode()
        } else {
            retval xor alg.lowercase().hashCode()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SecretKey) return false
        val thatAlg = other.getAlgorithm()
        if (!alg.equals(thatAlg, ignoreCase = true)) {
            val isTripleDesAlias = (alg.equals("DESede", ignoreCase = true) && thatAlg.equals("TripleDES", ignoreCase = true)) ||
                (alg.equals("TripleDES", ignoreCase = true) && thatAlg.equals("DESede", ignoreCase = true))
            if (!isTripleDesAlias) return false
        }
        val thatKey = other.getEncoded()
        return MessageDigest.isEqual(this.keyBytes, thatKey)
    }
}
