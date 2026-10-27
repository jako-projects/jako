package javax.crypto.spec

import java.security.spec.AlgorithmParameterSpec

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
