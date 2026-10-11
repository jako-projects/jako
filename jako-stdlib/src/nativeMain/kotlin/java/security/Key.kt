package java.security

import java.io.Serializable

actual interface Key : Serializable {
    actual fun getAlgorithm(): String
    actual fun getFormat(): String?
    actual fun getEncoded(): ByteArray?
    actual val encoded: ByteArray get() = getEncoded() ?: byteArrayOf()
}
