@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable

expect open class Random : Serializable {
    constructor()
    constructor(seed: Long)
    open fun setSeed(seed: Long)
    open fun nextInt(): Int
    open fun nextInt(bound: Int): Int
    open fun nextLong(): Long
    open fun nextBoolean(): Boolean
    open fun nextDouble(): Double
    open fun nextFloat(): Float
    open fun nextBytes(bytes: ByteArray)
}
