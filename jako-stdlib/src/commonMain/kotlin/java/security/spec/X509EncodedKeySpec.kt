@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.spec

expect open class X509EncodedKeySpec(encodedKey: ByteArray) : EncodedKeySpec {
    override fun getEncoded(): ByteArray
    override val encoded: ByteArray
    override fun getFormat(): String
    override val format: String
}
