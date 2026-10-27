package java.util

import java.io.Serializable

actual open class Random : Serializable {
    private var seed: Long

    actual constructor() : this(kotlin.random.Random.nextLong())
    actual constructor(seed: Long) {
        this.seed = (seed xor 0x5DEECE66DL) and ((1L shl 48) - 1)
    }

    actual open fun setSeed(seed: Long) {
        this.seed = (seed xor 0x5DEECE66DL) and ((1L shl 48) - 1)
    }

    protected open fun next(bits: Int): Int {
        seed = (seed * 0x5DEECE66DL + 0xBL) and ((1L shl 48) - 1)
        return (seed ushr (48 - bits)).toInt()
    }

    actual open fun nextInt(): Int = next(32)

    actual open fun nextInt(bound: Int): Int {
        require(bound > 0) { "bound must be positive" }
        var r = next(31)
        val m = bound - 1
        if (bound and m == 0) {
            r = ((bound.toLong() * r.toLong()) shr 31).toInt()
        } else {
            var u = r
            while (u - (u % bound) + m < 0) {
                u = next(31)
            }
            r = u % bound
        }
        return r
    }

    actual open fun nextLong(): Long = (next(32).toLong() shl 32) + next(32)

    actual open fun nextBoolean(): Boolean = next(1) != 0

    actual open fun nextDouble(): Double =
        ((next(26).toLong() shl 27) + next(27)) / (1L shl 53).toDouble()

    actual open fun nextFloat(): Float = next(24) / (1 shl 24).toFloat()

    actual open fun nextBytes(bytes: ByteArray) {
        var i = 0
        val len = bytes.size
        while (i < len) {
            var rnd = nextInt()
            var n = minOf(len - i, 4)
            while (n-- > 0) {
                bytes[i++] = rnd.toByte()
                rnd = rnd ushr 8
            }
        }
    }
}
