@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.crypto.spec

import java.security.spec.KeySpec
import javax.crypto.SecretKey

/**
 * [Nullability Decision] SecretKeySpec methods return non-null String ("RAW" for format) and non-null ByteArray.
 */
expect open class SecretKeySpec : KeySpec, SecretKey {
    constructor(key: ByteArray, algorithm: String)
    constructor(key: ByteArray, offset: Int, len: Int, algorithm: String)

    override fun getAlgorithm(): String
    override val algorithm: String
    override fun getFormat(): String
    override val format: String
    override fun getEncoded(): ByteArray
    override val encoded: ByteArray
}
