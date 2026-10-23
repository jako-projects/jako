@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security

import java.nio.ByteBuffer

expect abstract class MessageDigest {
    protected constructor(algorithm: String)

    open fun update(input: Byte)
    open fun update(input: ByteArray)
    abstract fun update(input: ByteArray, offset: Int, len: Int)
    open fun update(input: ByteBuffer)
    abstract fun digest(): ByteArray
    open fun digest(buf: ByteArray, offset: Int, len: Int): Int
    open fun digest(input: ByteArray): ByteArray
    abstract fun reset()
    fun getAlgorithm(): String
    val algorithm: String
    open fun getDigestLength(): Int
    val digestLength: Int

    companion object {
        fun getInstance(algorithm: String): MessageDigest
        fun isEqual(digesta: ByteArray?, digestb: ByteArray?): Boolean
    }
}
