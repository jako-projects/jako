@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security

import java.util.Random

expect open class GeneralSecurityException : Exception {
    constructor()
    constructor(msg: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect open class NoSuchAlgorithmException : GeneralSecurityException {
    constructor()
    constructor(msg: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect abstract class MessageDigest {
    protected constructor(algorithm: String)

    open fun update(input: Byte)
    open fun update(input: ByteArray)
    abstract fun update(input: ByteArray, offset: Int, len: Int)
    abstract fun digest(): ByteArray
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

expect open class SecureRandom() : Random {
    override fun nextBytes(bytes: ByteArray)

    companion object {
        fun getInstance(algorithm: String): SecureRandom
    }
}
