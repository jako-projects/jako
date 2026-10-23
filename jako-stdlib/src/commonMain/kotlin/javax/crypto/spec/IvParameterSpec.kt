@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.crypto.spec

import java.security.spec.AlgorithmParameterSpec

/**
 * [Nullability Decision] IvParameterSpec.getIV() returns non-null ByteArray (cloned copy).
 */
expect open class IvParameterSpec : AlgorithmParameterSpec {
    constructor(iv: ByteArray)
    constructor(iv: ByteArray, offset: Int, len: Int)

    fun getIV(): ByteArray
    val iv: ByteArray
}
