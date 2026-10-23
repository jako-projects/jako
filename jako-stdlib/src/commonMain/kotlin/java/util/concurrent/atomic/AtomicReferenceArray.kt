@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.BinaryOperator
import java.util.function.UnaryOperator

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
