@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.spec

/**
 * [Nullability Decision] EncodedKeySpec.getEncoded() returns non-null cloned ByteArray.
 */
expect abstract class EncodedKeySpec(encodedKey: ByteArray) : KeySpec {
    open fun getEncoded(): ByteArray
    open val encoded: ByteArray
    abstract fun getFormat(): String
    open val format: String
}
