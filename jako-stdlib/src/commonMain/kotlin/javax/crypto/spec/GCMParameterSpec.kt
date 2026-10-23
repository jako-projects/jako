@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.crypto.spec

import java.security.spec.AlgorithmParameterSpec

/**
 * [Nullability Decision] GCMParameterSpec.getIV() returns non-null cloned ByteArray; tLen is tag length in bits.
 */
expect open class GCMParameterSpec : AlgorithmParameterSpec {
    constructor(tLen: Int, src: ByteArray)
    constructor(tLen: Int, src: ByteArray, offset: Int, len: Int)

    fun getTLen(): Int
    val tLen: Int
    fun getIV(): ByteArray
    val iv: ByteArray
}
