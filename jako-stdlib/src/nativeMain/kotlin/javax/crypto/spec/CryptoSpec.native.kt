package javax.crypto.spec

import java.security.MessageDigest
import java.security.spec.AlgorithmParameterSpec
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

actual open class IvParameterSpec : AlgorithmParameterSpec {
    private val ivBytes: ByteArray

    actual constructor(iv: ByteArray) : this(iv, 0, iv.size)

    actual constructor(iv: ByteArray, offset: Int, len: Int) {
        if (offset < 0 || len < 0) {
            throw IndexOutOfBoundsException("offset or len is negative")
        }
        if (iv.size - offset < len) {
            throw IllegalArgumentException("IV buffer too short for given offset/length combination")
        }
        this.ivBytes = iv.copyOfRange(offset, offset + len)
    }

    actual fun getIV(): ByteArray = ivBytes.copyOf()
    actual val iv: ByteArray get() = getIV()
}

actual open class GCMParameterSpec : AlgorithmParameterSpec {
    private val tagLenBits: Int
    private val ivBytes: ByteArray

    actual constructor(tLen: Int, src: ByteArray) : this(tLen, src, 0, src.size)

    actual constructor(tLen: Int, src: ByteArray, offset: Int, len: Int) {
        if (tLen < 0) {
            throw IllegalArgumentException("Length argument is negative")
        }
        if (offset < 0 || len < 0 || src.size - offset < len) {
            throw IllegalArgumentException("Invalid buffer arguments")
        }
        this.tagLenBits = tLen
        this.ivBytes = src.copyOfRange(offset, offset + len)
    }

    actual fun getTLen(): Int = tagLenBits
    actual val tLen: Int get() = tagLenBits

    actual fun getIV(): ByteArray = ivBytes.copyOf()
    actual val iv: ByteArray get() = getIV()
}

actual open class PBEKeySpec : KeySpec {
    private var passwd: CharArray?
    private val saltBytes: ByteArray?
    private val iterCount: Int
    private val keyLen: Int

    actual constructor(password: CharArray?) {
        this.passwd = password?.copyOf() ?: CharArray(0)
        this.saltBytes = null
        this.iterCount = 0
        this.keyLen = 0
    }

    actual constructor(password: CharArray?, salt: ByteArray, iterationCount: Int, keyLength: Int) {
        if (salt.isEmpty()) throw IllegalArgumentException("the salt parameter must not be empty")
        if (iterationCount <= 0) throw IllegalArgumentException("invalid iterationCount value")
        if (keyLength <= 0) throw IllegalArgumentException("invalid keyLength value")
        this.passwd = password?.copyOf() ?: CharArray(0)
        this.saltBytes = salt.copyOf()
        this.iterCount = iterationCount
        this.keyLen = keyLength
    }

    actual constructor(password: CharArray?, salt: ByteArray, iterationCount: Int) {
        if (salt.isEmpty()) throw IllegalArgumentException("the salt parameter must not be empty")
        if (iterationCount <= 0) throw IllegalArgumentException("invalid iterationCount value")
        this.passwd = password?.copyOf() ?: CharArray(0)
        this.saltBytes = salt.copyOf()
        this.iterCount = iterationCount
        this.keyLen = 0
    }

    actual fun clearPassword() {
        passwd?.fill(' ')
        passwd = null
    }

    actual fun getPassword(): CharArray {
        val p = passwd ?: throw IllegalStateException("password has been cleared")
        return p.copyOf()
    }

    actual val password: CharArray get() = getPassword()

    actual fun getSalt(): ByteArray? = saltBytes?.copyOf()
    actual val salt: ByteArray? get() = getSalt()

    actual fun getIterationCount(): Int = iterCount
    actual val iterationCount: Int get() = iterCount

    actual fun getKeyLength(): Int = keyLen
    actual val keyLength: Int get() = keyLen
}
