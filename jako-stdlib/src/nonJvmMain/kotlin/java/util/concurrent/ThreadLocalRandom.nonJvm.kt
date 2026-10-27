package java.util.concurrent

import java.lang.ThreadLocal
import java.util.Random
import kotlin.concurrent.withLock

actual class ThreadLocalRandom private constructor(
    initialSeed: Long
) : Random(initialSeed) {
    private var initialized = true

    actual override fun setSeed(seed: Long) {
        if (initialized) {
            throw UnsupportedOperationException()
        }
        super.setSeed(seed)
    }

    actual override fun nextBoolean(): Boolean = super.nextBoolean()

    actual override fun nextInt(): Int = super.nextInt()

    actual override fun nextInt(bound: Int): Int {
        if (bound <= 0) throw IllegalArgumentException("bound must be positive")
        return super.nextInt(bound)
    }

    actual fun nextInt(origin: Int, bound: Int): Int {
        if (origin >= bound) throw IllegalArgumentException("bound must be greater than origin")
        val n = bound - origin
        if (n > 0) {
            return nextInt(n) + origin
        }
        var r: Int
        do {
            r = nextInt()
        } while (r < origin || r >= bound)
        return r
    }

    actual override fun nextLong(): Long = super.nextLong()

    actual fun nextLong(bound: Long): Long {
        if (bound <= 0L) throw IllegalArgumentException("bound must be positive")
        var r = nextLong() ushr 1
        val m = bound - 1L
        if ((bound and m) == 0L) {
            r = r and m
        } else {
            var u = r
            while (u - (u % bound) + m < 0L) {
                u = nextLong() ushr 1
            }
            r = u % bound
        }
        return r
    }

    actual fun nextLong(origin: Long, bound: Long): Long {
        if (origin >= bound) throw IllegalArgumentException("bound must be greater than origin")
        val n = bound - origin
        if (n > 0L) {
            return nextLong(n) + origin
        }
        var r: Long
        do {
            r = nextLong()
        } while (r < origin || r >= bound)
        return r
    }

    actual override fun nextFloat(): Float = super.nextFloat()

    actual override fun nextDouble(): Double = super.nextDouble()

    actual fun nextDouble(bound: Double): Double {
        if (!(bound > 0.0)) throw IllegalArgumentException("bound must be positive")
        val result = nextDouble() * bound
        return if (result < bound) result else Double.fromBits(bound.toRawBits() - 1)
    }

    actual fun nextDouble(origin: Double, bound: Double): Double {
        if (!(origin < bound)) throw IllegalArgumentException("bound must be greater than origin")
        var r = nextDouble() * (bound - origin) + origin
        if (r >= bound) {
            r = Double.fromBits(bound.toRawBits() - 1)
        }
        return r
    }

    actual companion object {
        private val localInstance = ThreadLocal.withInitial {
            ThreadLocalRandom(kotlin.random.Random.nextLong())
        }

        actual fun current(): ThreadLocalRandom = localInstance.get()
    }
}
