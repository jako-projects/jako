@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.IntBinaryOperator
import java.util.function.IntUnaryOperator

expect open class AtomicIntegerArray : Serializable {
    constructor(length: Int)
    constructor(array: IntArray)

    fun length(): Int
    fun get(i: Int): Int
    fun set(i: Int, newValue: Int)
    fun lazySet(i: Int, newValue: Int)
    fun getAndSet(i: Int, newValue: Int): Int
    fun compareAndSet(i: Int, expect: Int, update: Int): Boolean
    fun weakCompareAndSet(i: Int, expect: Int, update: Int): Boolean
    fun getAndIncrement(i: Int): Int
    fun getAndDecrement(i: Int): Int
    fun getAndAdd(i: Int, delta: Int): Int
    fun incrementAndGet(i: Int): Int
    fun decrementAndGet(i: Int): Int
    fun addAndGet(i: Int, delta: Int): Int
    fun getAndUpdate(i: Int, updateFunction: IntUnaryOperator): Int
    fun updateAndGet(i: Int, updateFunction: IntUnaryOperator): Int
    fun getAndAccumulate(i: Int, x: Int, accumulatorFunction: IntBinaryOperator): Int
    fun accumulateAndGet(i: Int, x: Int, accumulatorFunction: IntBinaryOperator): Int
    override fun toString(): String
}
