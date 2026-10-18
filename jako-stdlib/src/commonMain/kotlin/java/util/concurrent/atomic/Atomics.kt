@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.BinaryOperator
import java.util.function.IntBinaryOperator
import java.util.function.IntUnaryOperator
import java.util.function.LongBinaryOperator
import java.util.function.LongUnaryOperator
import java.util.function.UnaryOperator

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

expect class AtomicBoolean(initialValue: Boolean) : Serializable {
    constructor()
    fun get(): Boolean
    fun set(newValue: Boolean)
    fun lazySet(newValue: Boolean)
    fun getAndSet(newValue: Boolean): Boolean
    fun compareAndSet(expect: Boolean, update: Boolean): Boolean
    fun weakCompareAndSet(expect: Boolean, update: Boolean): Boolean
}

/**
 * [Nullability Decision] Java `AtomicReference.get/set` accept and return a nullable referent
 * (the no-arg constructor stores `null`). Kotlin signatures use `V?`.
 */
expect class AtomicReference<V>(initialValue: V?) : Serializable {
    constructor()
    fun get(): V?
    fun set(newValue: V?)
    fun lazySet(newValue: V?)
    fun getAndSet(newValue: V?): V?
    fun compareAndSet(expect: V?, update: V?): Boolean
    fun weakCompareAndSet(expect: V?, update: V?): Boolean
    fun getAndUpdate(updateFunction: UnaryOperator<V?>): V?
    fun updateAndGet(updateFunction: UnaryOperator<V?>): V?
    fun getAndAccumulate(x: V?, accumulatorFunction: BinaryOperator<V?>): V?
    fun accumulateAndGet(x: V?, accumulatorFunction: BinaryOperator<V?>): V?
}

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

/**
 * [Nullability Decision] AtomicReferenceArray elements may be null (initialized to null when constructed with length).
 */
expect open class AtomicReferenceArray<E> : Serializable {
    constructor(length: Int)
    constructor(array: Array<E>)

    fun length(): Int
    fun get(i: Int): E?
    fun set(i: Int, newValue: E?)
    fun lazySet(i: Int, newValue: E?)
    fun getAndSet(i: Int, newValue: E?): E?
    fun compareAndSet(i: Int, expect: E?, update: E?): Boolean
    fun weakCompareAndSet(i: Int, expect: E?, update: E?): Boolean
    fun getAndUpdate(i: Int, updateFunction: UnaryOperator<E?>): E?
    fun updateAndGet(i: Int, updateFunction: UnaryOperator<E?>): E?
    fun getAndAccumulate(i: Int, x: E?, accumulatorFunction: BinaryOperator<E?>): E?
    fun accumulateAndGet(i: Int, x: E?, accumulatorFunction: BinaryOperator<E?>): E?
    override fun toString(): String
}

/**
 * [Nullability Decision] AtomicStampedReference reference is nullable (`V?`), stampHolder is non-null `IntArray` of length >= 1.
 */
expect open class AtomicStampedReference<V>(initialRef: V?, initialStamp: Int) {
    open fun getReference(): V?
    open fun getStamp(): Int
    open val reference: V?
    open val stamp: Int
    open fun get(stampHolder: IntArray): V?
    open fun weakCompareAndSet(expectedReference: V?, newReference: V?, expectedStamp: Int, newStamp: Int): Boolean
    open fun compareAndSet(expectedReference: V?, newReference: V?, expectedStamp: Int, newStamp: Int): Boolean
    open fun set(newReference: V?, newStamp: Int)
    open fun attemptStamp(expectedReference: V?, newStamp: Int): Boolean
}

/**
 * [Nullability Decision] AtomicMarkableReference reference is nullable (`V?`), markHolder is non-null `BooleanArray` of length >= 1.
 */
expect open class AtomicMarkableReference<V>(initialRef: V?, initialMark: Boolean) {
    open fun getReference(): V?
    open fun isMarked(): Boolean
    open val reference: V?
    open val isMarked: Boolean
    open fun get(markHolder: BooleanArray): V?
    open fun weakCompareAndSet(expectedReference: V?, newReference: V?, expectedMark: Boolean, newMark: Boolean): Boolean
    open fun compareAndSet(expectedReference: V?, newReference: V?, expectedMark: Boolean, newMark: Boolean): Boolean
    open fun set(newReference: V?, newMark: Boolean)
    open fun attemptMark(expectedReference: V?, newMark: Boolean): Boolean
}



