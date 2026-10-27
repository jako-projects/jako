package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.IntBinaryOperator
import java.util.function.IntUnaryOperator

actual class AtomicInteger actual constructor(initialValue: Int) : Number(), Serializable {
    actual constructor() : this(0)

    private var value: Int = initialValue

    actual fun get(): Int = value

    actual fun set(newValue: Int) {
        value = newValue
    }

    actual fun lazySet(newValue: Int) {
        value = newValue
    }

    actual fun getAndSet(newValue: Int): Int {
        val prev = value
        value = newValue
        return prev
    }

    actual fun compareAndSet(expect: Int, update: Int): Boolean {
        if (value == expect) {
            value = update
            return true
        }
        return false
    }

    actual fun weakCompareAndSet(expect: Int, update: Int): Boolean =
        compareAndSet(expect, update)

    actual fun getAndIncrement(): Int = getAndAdd(1)

    actual fun getAndDecrement(): Int = getAndAdd(-1)

    actual fun getAndAdd(delta: Int): Int {
        val prev = value
        value = prev + delta
        return prev
    }

    actual fun incrementAndGet(): Int = addAndGet(1)

    actual fun decrementAndGet(): Int = addAndGet(-1)

    actual fun addAndGet(delta: Int): Int {
        value += delta
        return value
    }

    actual fun getAndUpdate(updateFunction: IntUnaryOperator): Int {
        val prev = value
        value = updateFunction.applyAsInt(prev)
        return prev
    }

    actual fun updateAndGet(updateFunction: IntUnaryOperator): Int {
        val next = updateFunction.applyAsInt(value)
        value = next
        return next
    }

    actual fun getAndAccumulate(x: Int, accumulatorFunction: IntBinaryOperator): Int {
        val prev = value
        value = accumulatorFunction.applyAsInt(prev, x)
        return prev
    }

    actual fun accumulateAndGet(x: Int, accumulatorFunction: IntBinaryOperator): Int {
        val next = accumulatorFunction.applyAsInt(value, x)
        value = next
        return next
    }

    override fun toString(): String = value.toString()

    actual override fun toByte(): Byte = value.toByte()
    actual override fun toShort(): Short = value.toShort()
    actual override fun toInt(): Int = value
    actual override fun toLong(): Long = value.toLong()
    actual override fun toFloat(): Float = value.toFloat()
    actual override fun toDouble(): Double = value.toDouble()
}
