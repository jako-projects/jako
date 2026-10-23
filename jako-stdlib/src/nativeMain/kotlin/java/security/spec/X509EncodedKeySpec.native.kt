package java.security.spec

actual open class X509EncodedKeySpec actual constructor(encodedKey: ByteArray) : EncodedKeySpec(encodedKey) {
    actual override fun getEncoded(): ByteArray = super.getEncoded()
    actual override val encoded: ByteArray get() = getEncoded()
    actual override fun getFormat(): String = "X.509"
    actual override val format: String get() = "X.509"
}
