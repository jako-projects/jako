package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.LongBinaryOperator
import java.util.function.LongUnaryOperator

actual class AtomicLong actual constructor(initialValue: Long) : Number(), Serializable {
    actual constructor() : this(0L)

    private var value: Long = initialValue

    actual fun get(): Long = value

    actual fun set(newValue: Long) {
        value = newValue
    }

    actual fun lazySet(newValue: Long) {
        value = newValue
    }

    actual fun getAndSet(newValue: Long): Long {
        val prev = value
        value = newValue
        return prev
    }

    actual fun compareAndSet(expect: Long, update: Long): Boolean {
        if (value == expect) {
            value = update
            return true
        }
        return false
    }

    actual fun weakCompareAndSet(expect: Long, update: Long): Boolean =
        compareAndSet(expect, update)

    actual fun getAndIncrement(): Long = getAndAdd(1L)

    actual fun getAndDecrement(): Long = getAndAdd(-1L)

    actual fun getAndAdd(delta: Long): Long {
        val prev = value
        value = prev + delta
        return prev
    }

    actual fun incrementAndGet(): Long = addAndGet(1L)

    actual fun decrementAndGet(): Long = addAndGet(-1L)

    actual fun addAndGet(delta: Long): Long {
        value += delta
        return value
    }

    actual fun getAndUpdate(updateFunction: LongUnaryOperator): Long {
        val prev = value
        value = updateFunction.applyAsLong(prev)
        return prev
    }

    actual fun updateAndGet(updateFunction: LongUnaryOperator): Long {
        val next = updateFunction.applyAsLong(value)
        value = next
        return next
    }

    actual fun getAndAccumulate(x: Long, accumulatorFunction: LongBinaryOperator): Long {
        val prev = value
        value = accumulatorFunction.applyAsLong(prev, x)
        return prev
    }

    actual fun accumulateAndGet(x: Long, accumulatorFunction: LongBinaryOperator): Long {
        val next = accumulatorFunction.applyAsLong(value, x)
        value = next
        return next
    }

    override fun toString(): String = value.toString()

    actual override fun toByte(): Byte = value.toByte()
    actual override fun toShort(): Short = value.toShort()
    actual override fun toInt(): Int = value.toInt()
    actual override fun toLong(): Long = value
    actual override fun toFloat(): Float = value.toFloat()
    actual override fun toDouble(): Double = value.toDouble()
}
