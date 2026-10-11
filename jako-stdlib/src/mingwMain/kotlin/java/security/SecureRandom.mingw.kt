@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.security

import kotlinx.cinterop.*
import platform.posix.*

actual open class SecureRandom actual constructor() : java.util.Random() {

    actual override fun nextBytes(bytes: ByteArray) {
        if (bytes.isEmpty()) return
        memScoped {
            val buf = allocArray<ByteVar>(bytes.size)
            var i = 0
            while (i < bytes.size) {
                val r = rand()
                val byteVal = (r and 0xFF).toByte()
                buf[i] = byteVal
                bytes[i] = byteVal
                i++
            }
        }
    }

    override fun nextInt(): Int {
        val bytes = ByteArray(4)
        nextBytes(bytes)
        return ((bytes[0].toInt() and 0xFF) shl 24) or
                ((bytes[1].toInt() and 0xFF) shl 16) or
                ((bytes[2].toInt() and 0xFF) shl 8) or
                (bytes[3].toInt() and 0xFF)
    }

    override fun nextInt(bound: Int): Int {
        require(bound > 0) { "bound must be positive" }
        val r = nextInt() and 0x7FFFFFFF
        return r % bound
    }

    override fun nextLong(): Long {
        val bytes = ByteArray(8)
        nextBytes(bytes)
        var result = 0L
        for (i in 0 until 8) {
            result = (result shl 8) or (bytes[i].toLong() and 0xFFL)
        }
        return result
    }

    override fun nextBoolean(): Boolean {
        return (nextInt() and 1) != 0
    }

    actual companion object {
        actual fun getInstance(algorithm: String): SecureRandom {
            return SecureRandom()
        }
    }
}
