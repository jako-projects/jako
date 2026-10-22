@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.atomic

import java.io.Serializable

expect open class DoubleAdder : Number, Serializable {
    constructor()
    open fun add(x: Double)
    open fun sum(): Double
    open fun reset()
    open fun sumThenReset(): Double

    override fun toByte(): Byte
    override fun toShort(): Short
    override fun toInt(): Int
    override fun toLong(): Long
    override fun toFloat(): Float
    override fun toDouble(): Double
}
