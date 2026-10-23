package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.LongBinaryOperator
import java.util.function.LongUnaryOperator

actual class AtomicLong actual constructor(initialValue: Long) : Number(), Serializable {
    actual constructor() : this(0L)

    private val delegate = kotlin.concurrent.AtomicLong(initialValue)

    actual fun get(): Long = delegate.value

    actual fun set(newValue: Long) {
        delegate.value = newValue
    }

    actual fun lazySet(newValue: Long) {
        delegate.value = newValue
    }

    actual fun getAndSet(newValue: Long): Long {
        var current: Long
        do {
            current = delegate.value
        } while (!delegate.compareAndSet(current, newValue))
        return current
    }

    actual fun compareAndSet(expect: Long, update: Long): Boolean {
        return delegate.compareAndSet(expect, update)
    }

    actual fun weakCompareAndSet(expect: Long, update: Long): Boolean {
        return delegate.compareAndSet(expect, update)
    }

    actual fun getAndIncrement(): Long = delegate.addAndGet(1L) - 1L

    actual fun getAndDecrement(): Long = delegate.addAndGet(-1L) + 1L

    actual fun getAndAdd(delta: Long): Long = delegate.addAndGet(delta) - delta

    actual fun incrementAndGet(): Long = delegate.addAndGet(1L)

    actual fun decrementAndGet(): Long = delegate.addAndGet(-1L)

    actual fun addAndGet(delta: Long): Long = delegate.addAndGet(delta)

    actual fun getAndUpdate(updateFunction: LongUnaryOperator): Long {
        var prev: Long
        var next: Long
        do {
            prev = get()
            next = updateFunction.applyAsLong(prev)
        } while (!compareAndSet(prev, next))
        return prev
    }

    actual fun updateAndGet(updateFunction: LongUnaryOperator): Long {
        var prev: Long
        var next: Long
        do {
            prev = get()
            next = updateFunction.applyAsLong(prev)
        } while (!compareAndSet(prev, next))
        return next
    }

    actual fun getAndAccumulate(x: Long, accumulatorFunction: LongBinaryOperator): Long {
        var prev: Long
        var next: Long
        do {
            prev = get()
            next = accumulatorFunction.applyAsLong(prev, x)
        } while (!compareAndSet(prev, next))
        return prev
    }

    actual fun accumulateAndGet(x: Long, accumulatorFunction: LongBinaryOperator): Long {
        var prev: Long
        var next: Long
        do {
            prev = get()
            next = accumulatorFunction.applyAsLong(prev, x)
        } while (!compareAndSet(prev, next))
        return next
    }

    override fun toString(): String = get().toString()

    actual override fun toByte(): Byte = get().toByte()
    actual override fun toShort(): Short = get().toShort()
    actual override fun toInt(): Int = get().toInt()
    actual override fun toLong(): Long = get()
    actual override fun toFloat(): Float = get().toFloat()
    actual override fun toDouble(): Double = get().toDouble()
}
