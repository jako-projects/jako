@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security

import java.nio.ByteBuffer

/**
 * [Nullability Decision] Signature methods follow JDK 8 nullability contracts:
 * - sign() returns non-null ByteArray.
 * - verify() takes non-null ByteArray and returns Boolean.
 */
expect abstract class Signature {
    fun getAlgorithm(): String
    val algorithm: String
    fun initVerify(publicKey: PublicKey)
    fun initSign(privateKey: PrivateKey)
    fun initSign(privateKey: PrivateKey, random: SecureRandom)
    fun sign(): ByteArray
    fun sign(outbuf: ByteArray, offset: Int, len: Int): Int
    fun verify(signature: ByteArray): Boolean
    fun verify(signature: ByteArray, offset: Int, length: Int): Boolean
    fun update(b: Byte)
    fun update(data: ByteArray)
    fun update(data: ByteArray, off: Int, len: Int)
    fun update(data: ByteBuffer)

    companion object {
        fun getInstance(algorithm: String): Signature
    }
}
