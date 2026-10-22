@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.BinaryOperator
import java.util.function.UnaryOperator

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
