@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.util.Random

/**
 * [Nullability Decision] ThreadLocalRandom.current() returns non-null ThreadLocalRandom.
 */
expect class ThreadLocalRandom : Random {
    override fun setSeed(seed: Long)
    override fun nextBoolean(): Boolean
    override fun nextInt(): Int
    override fun nextInt(bound: Int): Int
    fun nextInt(origin: Int, bound: Int): Int
    override fun nextLong(): Long
    fun nextLong(bound: Long): Long
    fun nextLong(origin: Long, bound: Long): Long
    override fun nextFloat(): Float
    override fun nextDouble(): Double
    fun nextDouble(bound: Double): Double
    fun nextDouble(origin: Double, bound: Double): Double

    companion object {
        fun current(): ThreadLocalRandom
    }
}
