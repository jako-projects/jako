@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.IntBinaryOperator
import java.util.function.IntUnaryOperator

expect class AtomicInteger(initialValue: Int) : Number, Serializable {
    constructor()
    fun get(): Int
    fun set(newValue: Int)
    fun lazySet(newValue: Int)
    fun getAndSet(newValue: Int): Int
    fun compareAndSet(expect: Int, update: Int): Boolean
    fun weakCompareAndSet(expect: Int, update: Int): Boolean
    fun getAndIncrement(): Int
    fun getAndDecrement(): Int
    fun getAndAdd(delta: Int): Int
    fun incrementAndGet(): Int
    fun decrementAndGet(): Int
    fun addAndGet(delta: Int): Int
    fun getAndUpdate(updateFunction: IntUnaryOperator): Int
    fun updateAndGet(updateFunction: IntUnaryOperator): Int
    fun getAndAccumulate(x: Int, accumulatorFunction: IntBinaryOperator): Int
    fun accumulateAndGet(x: Int, accumulatorFunction: IntBinaryOperator): Int

    override fun toByte(): Byte
    override fun toShort(): Short
    override fun toInt(): Int
    override fun toLong(): Long
    override fun toFloat(): Float
    override fun toDouble(): Double
}
