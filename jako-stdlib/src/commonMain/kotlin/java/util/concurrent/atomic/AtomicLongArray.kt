@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.LongBinaryOperator
import java.util.function.LongUnaryOperator

expect open class AtomicLongArray : Serializable {
    constructor(length: Int)
    constructor(array: LongArray)

    fun length(): Int
    fun get(i: Int): Long
    fun set(i: Int, newValue: Long)
    fun lazySet(i: Int, newValue: Long)
    fun getAndSet(i: Int, newValue: Long): Long
    fun compareAndSet(i: Int, expect: Long, update: Long): Boolean
    fun weakCompareAndSet(i: Int, expect: Long, update: Long): Boolean
    fun getAndIncrement(i: Int): Long
    fun getAndDecrement(i: Int): Long
    fun getAndAdd(i: Int, delta: Long): Long
    fun incrementAndGet(i: Int): Long
    fun decrementAndGet(i: Int): Long
    fun addAndGet(i: Int, delta: Long): Long
    fun getAndUpdate(i: Int, updateFunction: LongUnaryOperator): Long
    fun updateAndGet(i: Int, updateFunction: LongUnaryOperator): Long
    fun getAndAccumulate(i: Int, x: Long, accumulatorFunction: LongBinaryOperator): Long
    fun accumulateAndGet(i: Int, x: Long, accumulatorFunction: LongBinaryOperator): Long
    override fun toString(): String
}
