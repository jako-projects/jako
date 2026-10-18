package java.security

import java.io.Serializable

actual interface Key : Serializable {
    actual fun getAlgorithm(): String
    actual fun getFormat(): String?
    actual fun getEncoded(): ByteArray?
    actual val algorithm: String get() = getAlgorithm()
    actual val format: String? get() = getFormat()
    actual val encoded: ByteArray get() = getEncoded() ?: byteArrayOf()
}
