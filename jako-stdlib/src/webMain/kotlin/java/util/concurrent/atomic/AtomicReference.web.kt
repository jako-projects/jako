package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.BinaryOperator
import java.util.function.UnaryOperator

actual class AtomicReference<V> actual constructor(initialValue: V?) : Serializable {
    actual constructor() : this(null)

    private var value: V? = initialValue

    actual fun get(): V? = value

    actual fun set(newValue: V?) {
        value = newValue
    }

    actual fun lazySet(newValue: V?) {
        value = newValue
    }

    actual fun getAndSet(newValue: V?): V? {
        val prev = value
        value = newValue
        return prev
    }

    actual fun compareAndSet(expect: V?, update: V?): Boolean {
        if (value === expect) {
            value = update
            return true
        }
        return false
    }

    actual fun weakCompareAndSet(expect: V?, update: V?): Boolean =
        compareAndSet(expect, update)

    actual fun getAndUpdate(updateFunction: UnaryOperator<V?>): V? {
        val prev = value
        value = updateFunction.apply(prev)
        return prev
    }

    actual fun updateAndGet(updateFunction: UnaryOperator<V?>): V? {
        val next = updateFunction.apply(value)
        value = next
        return next
    }

    actual fun getAndAccumulate(x: V?, accumulatorFunction: BinaryOperator<V?>): V? {
        val prev = value
        value = accumulatorFunction.apply(prev, x)
        return prev
    }

    actual fun accumulateAndGet(x: V?, accumulatorFunction: BinaryOperator<V?>): V? {
        val next = accumulatorFunction.apply(value, x)
        value = next
        return next
    }

    override fun toString(): String = value.toString()
}
