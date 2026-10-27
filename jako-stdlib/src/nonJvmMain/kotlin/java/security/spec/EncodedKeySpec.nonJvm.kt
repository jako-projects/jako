package java.security.spec

actual abstract class EncodedKeySpec actual constructor(encodedKey: ByteArray) : KeySpec {
    private val keyBytes: ByteArray = encodedKey.copyOf()

    actual open fun getEncoded(): ByteArray = keyBytes.copyOf()
    actual open val encoded: ByteArray get() = getEncoded()
    actual abstract fun getFormat(): String
    actual open val format: String get() = getFormat()
}
