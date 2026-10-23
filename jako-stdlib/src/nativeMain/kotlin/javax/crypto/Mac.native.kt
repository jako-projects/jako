package javax.crypto

import java.io.ByteArrayOutputStream
import java.lang.Cloneable
import java.nio.ByteBuffer
import java.security.*
import java.security.spec.*

actual open class Mac private constructor(
    private val algName: String,
    private val digestAlg: String,
    private val blockSize: Int,
    private val macLen: Int
) : Cloneable {
    private var initialized = false
    private var ipad = ByteArray(0)
    private var opad = ByteArray(0)
    private var buffer = ByteArrayOutputStream()

    actual fun getAlgorithm(): String = algName
    actual val algorithm: String get() = algName

    actual fun getMacLength(): Int = macLen
    actual val macLength: Int get() = macLen

    actual fun init(key: Key) {
        init(key, null)
    }

    actual fun init(key: Key, params: AlgorithmParameterSpec?) {
        if (params != null) {
            throw InvalidAlgorithmParameterException("HMAC does not use parameters")
        }
        val keyBytes = key.getEncoded() ?: throw InvalidKeyException("Missing key data")
        val k0 = if (keyBytes.size > blockSize) {
            MessageDigest.getInstance(digestAlg).digest(keyBytes)
        } else {
            keyBytes
        }
        ipad = ByteArray(blockSize) { i ->
            val b = if (i < k0.size) k0[i].toInt() and 0xFF else 0
            (b xor 0x36).toByte()
        }
        opad = ByteArray(blockSize) { i ->
            val b = if (i < k0.size) k0[i].toInt() and 0xFF else 0
            (b xor 0x5c).toByte()
        }
        buffer.reset()
        initialized = true
    }

    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("MAC not initialized")
        }
    }

    actual fun update(input: Byte) {
        checkInitialized()
        buffer.write(input.toInt() and 0xFF)
    }

    actual fun update(input: ByteArray?) {
        checkInitialized()
        if (input != null && input.isNotEmpty()) {
            buffer.write(input, 0, input.size)
        }
    }

    actual fun update(input: ByteArray?, offset: Int, len: Int) {
        checkInitialized()
        if (input == null) {
            throw IllegalArgumentException("Input buffer must not be null")
        }
        if (offset < 0 || len < 0 || offset > input.size - len) {
            throw IllegalArgumentException("Bad arguments")
        }
        if (len > 0) {
            buffer.write(input, offset, len)
        }
    }

    actual fun update(input: ByteBuffer) {
        checkInitialized()
        val rem = input.remaining()
        if (rem > 0) {
            val tmp = ByteArray(rem)
            input.get(tmp)
            buffer.write(tmp, 0, rem)
        }
    }

    actual fun doFinal(): ByteArray {
        checkInitialized()
        val data = buffer.toByteArray()
        buffer.reset()

        val innerMd = MessageDigest.getInstance(digestAlg)
        innerMd.update(ipad)
        innerMd.update(data)
        val innerHash = innerMd.digest()

        val outerMd = MessageDigest.getInstance(digestAlg)
        outerMd.update(opad)
        outerMd.update(innerHash)
        return outerMd.digest()
    }

    actual fun doFinal(output: ByteArray, outOffset: Int) {
        checkInitialized()
        if (outOffset < 0 || output.size - outOffset < macLen) {
            throw ShortBufferException("Cannot store MAC in output buffer")
        }
        val mac = doFinal()
        mac.copyInto(output, destinationOffset = outOffset)
    }

    actual fun doFinal(input: ByteArray?): ByteArray {
        checkInitialized()
        update(input)
        return doFinal()
    }

    actual fun reset() {
        buffer.reset()
    }

    open fun clone(): Any {
        val copy = Mac(algName, digestAlg, blockSize, macLen)
        copy.initialized = this.initialized
        copy.ipad = this.ipad.copyOf()
        copy.opad = this.opad.copyOf()
        val currentBytes = this.buffer.toByteArray()
        copy.buffer.write(currentBytes, 0, currentBytes.size)
        return copy
    }

    actual companion object {
        actual fun getInstance(algorithm: String): Mac {
            val normalized = algorithm.uppercase().replace("-", "")
            return when (normalized) {
                "HMACMD5" -> Mac(algorithm, "MD5", 64, 16)
                "HMACSHA1" -> Mac(algorithm, "SHA-1", 64, 20)
                "HMACSHA256" -> Mac(algorithm, "SHA-256", 64, 32)
                "HMACSHA384" -> Mac(algorithm, "SHA-384", 128, 48)
                "HMACSHA512" -> Mac(algorithm, "SHA-512", 128, 64)
                else -> throw NoSuchAlgorithmException("Algorithm $algorithm not available")
            }
        }
    }
}
