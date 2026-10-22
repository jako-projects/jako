package java.security.spec

actual open class PKCS8EncodedKeySpec actual constructor(encodedKey: ByteArray) : EncodedKeySpec(encodedKey) {
    actual override fun getEncoded(): ByteArray = super.getEncoded()
    actual override val encoded: ByteArray get() = getEncoded()
    actual override fun getFormat(): String = "PKCS#8"
    actual override val format: String get() = "PKCS#8"
}
