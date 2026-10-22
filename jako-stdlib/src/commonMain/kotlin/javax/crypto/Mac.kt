@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.crypto

import java.lang.Cloneable
import java.nio.ByteBuffer
import java.security.Key
import java.security.spec.AlgorithmParameterSpec

/**
 * [Nullability Decision] Mac methods follow JDK 8 nullability contracts:
 * - getAlgorithm() returns non-null String.
 * - update(ByteArray?) and doFinal(ByteArray?) accept nullable ByteArray (no-op on null in JDK 8).
 * - doFinal() returns non-null ByteArray.
 */
expect open class Mac : Cloneable {
    fun getAlgorithm(): String
    val algorithm: String
    fun getMacLength(): Int
    val macLength: Int
    fun init(key: Key)
    fun init(key: Key, params: AlgorithmParameterSpec?)
    fun update(input: Byte)
    fun update(input: ByteArray?)
    fun update(input: ByteArray?, offset: Int, len: Int)
    fun update(input: ByteBuffer)
    fun doFinal(): ByteArray
    fun doFinal(output: ByteArray, outOffset: Int)
    fun doFinal(input: ByteArray?): ByteArray
    fun reset()

    companion object {
        fun getInstance(algorithm: String): Mac
    }
}
