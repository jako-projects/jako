package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.IntBinaryOperator
import java.util.function.IntUnaryOperator

actual class AtomicInteger actual constructor(initialValue: Int) : Number(), Serializable {
    actual constructor() : this(0)

    private val delegate = kotlin.concurrent.AtomicInt(initialValue)

    actual fun get(): Int = delegate.value

    actual fun set(newValue: Int) {
        delegate.value = newValue
    }

    actual fun lazySet(newValue: Int) {
        delegate.value = newValue
    }

    actual fun getAndSet(newValue: Int): Int {
        var current: Int
        do {
            current = delegate.value
        } while (!delegate.compareAndSet(current, newValue))
        return current
    }

    actual fun compareAndSet(expect: Int, update: Int): Boolean {
        return delegate.compareAndSet(expect, update)
    }

    actual fun weakCompareAndSet(expect: Int, update: Int): Boolean {
        return delegate.compareAndSet(expect, update)
    }

    actual fun getAndIncrement(): Int = delegate.addAndGet(1) - 1

    actual fun getAndDecrement(): Int = delegate.addAndGet(-1) + 1

    actual fun getAndAdd(delta: Int): Int = delegate.addAndGet(delta) - delta

    actual fun incrementAndGet(): Int = delegate.addAndGet(1)

    actual fun decrementAndGet(): Int = delegate.addAndGet(-1)

    actual fun addAndGet(delta: Int): Int = delegate.addAndGet(delta)

    actual fun getAndUpdate(updateFunction: IntUnaryOperator): Int {
        var prev: Int
        var next: Int
        do {
            prev = get()
            next = updateFunction.applyAsInt(prev)
        } while (!compareAndSet(prev, next))
        return prev
    }

    actual fun updateAndGet(updateFunction: IntUnaryOperator): Int {
        var prev: Int
        var next: Int
        do {
            prev = get()
            next = updateFunction.applyAsInt(prev)
        } while (!compareAndSet(prev, next))
        return next
    }

    actual fun getAndAccumulate(x: Int, accumulatorFunction: IntBinaryOperator): Int {
        var prev: Int
        var next: Int
        do {
            prev = get()
            next = accumulatorFunction.applyAsInt(prev, x)
        } while (!compareAndSet(prev, next))
        return prev
    }

    actual fun accumulateAndGet(x: Int, accumulatorFunction: IntBinaryOperator): Int {
        var prev: Int
        var next: Int
        do {
            prev = get()
            next = accumulatorFunction.applyAsInt(prev, x)
        } while (!compareAndSet(prev, next))
        return next
    }

    override fun toString(): String = get().toString()

    actual override fun toByte(): Byte = get().toByte()
    actual override fun toShort(): Short = get().toShort()
    actual override fun toInt(): Int = get()
    actual override fun toLong(): Long = get().toLong()
    actual override fun toFloat(): Float = get().toFloat()
    actual override fun toDouble(): Double = get().toDouble()
}
