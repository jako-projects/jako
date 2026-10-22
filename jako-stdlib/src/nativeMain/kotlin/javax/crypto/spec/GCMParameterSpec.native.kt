package javax.crypto.spec

import java.security.spec.AlgorithmParameterSpec

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
