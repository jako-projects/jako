@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.LongBinaryOperator
import java.util.function.LongUnaryOperator

expect class AtomicLong(initialValue: Long) : Number, Serializable {
    constructor()
    fun get(): Long
    fun set(newValue: Long)
    fun lazySet(newValue: Long)
    fun getAndSet(newValue: Long): Long
    fun compareAndSet(expect: Long, update: Long): Boolean
    fun weakCompareAndSet(expect: Long, update: Long): Boolean
    fun getAndIncrement(): Long
    fun getAndDecrement(): Long
    fun getAndAdd(delta: Long): Long
    fun incrementAndGet(): Long
    fun decrementAndGet(): Long
    fun addAndGet(delta: Long): Long
    fun getAndUpdate(updateFunction: LongUnaryOperator): Long
    fun updateAndGet(updateFunction: LongUnaryOperator): Long
    fun getAndAccumulate(x: Long, accumulatorFunction: LongBinaryOperator): Long
    fun accumulateAndGet(x: Long, accumulatorFunction: LongBinaryOperator): Long

    override fun toByte(): Byte
    override fun toShort(): Short
    override fun toInt(): Int
    override fun toLong(): Long
    override fun toFloat(): Float
    override fun toDouble(): Double
}
