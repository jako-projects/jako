package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.BinaryOperator
import java.util.function.UnaryOperator

actual class AtomicReference<V> actual constructor(initialValue: V?) : Serializable {
    actual constructor() : this(null)

    @Suppress("UNCHECKED_CAST")
    private val delegate = kotlin.concurrent.AtomicReference(initialValue as Any?)

    @Suppress("UNCHECKED_CAST")
    actual fun get(): V? = delegate.value as V?

    actual fun set(newValue: V?) {
        delegate.value = newValue
    }

    actual fun lazySet(newValue: V?) {
        delegate.value = newValue
    }

    @Suppress("UNCHECKED_CAST")
    actual fun getAndSet(newValue: V?): V? {
        var current: Any?
        do {
            current = delegate.value
        } while (!delegate.compareAndSet(current, newValue))
        return current as V?
    }

    actual fun compareAndSet(expect: V?, update: V?): Boolean {
        return delegate.compareAndSet(expect as Any?, update as Any?)
    }

    actual fun weakCompareAndSet(expect: V?, update: V?): Boolean {
        return delegate.compareAndSet(expect as Any?, update as Any?)
    }

    actual fun getAndUpdate(updateFunction: UnaryOperator<V?>): V? {
        var prev: V?
        var next: V?
        do {
            prev = get()
            next = updateFunction.apply(prev)
        } while (!compareAndSet(prev, next))
        return prev
    }

    actual fun updateAndGet(updateFunction: UnaryOperator<V?>): V? {
        var prev: V?
        var next: V?
        do {
            prev = get()
            next = updateFunction.apply(prev)
        } while (!compareAndSet(prev, next))
        return next
    }

    actual fun getAndAccumulate(x: V?, accumulatorFunction: BinaryOperator<V?>): V? {
        var prev: V?
        var next: V?
        do {
            prev = get()
            next = accumulatorFunction.apply(prev, x)
        } while (!compareAndSet(prev, next))
        return prev
    }

    actual fun accumulateAndGet(x: V?, accumulatorFunction: BinaryOperator<V?>): V? {
        var prev: V?
        var next: V?
        do {
            prev = get()
            next = accumulatorFunction.apply(prev, x)
        } while (!compareAndSet(prev, next))
        return next
    }

    override fun toString(): String = get().toString()
}
