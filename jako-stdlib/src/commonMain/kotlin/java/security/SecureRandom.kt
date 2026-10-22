@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security

import java.util.Random

expect open class SecureRandom() : Random {
    override fun nextBytes(bytes: ByteArray)

    companion object {
        fun getInstance(algorithm: String): SecureRandom
    }
}
