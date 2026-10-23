@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.atomic

import java.io.Serializable

expect open class LongAdder : Number, Serializable {
    constructor()
    open fun add(x: Long)
    open fun increment()
    open fun decrement()
    open fun sum(): Long
    open fun reset()
    open fun sumThenReset(): Long

    override fun toByte(): Byte
    override fun toShort(): Short
    override fun toInt(): Int
    override fun toLong(): Long
    override fun toFloat(): Float
    override fun toDouble(): Double
}
