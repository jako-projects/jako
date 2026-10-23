package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.IntBinaryOperator
import java.util.function.IntUnaryOperator

actual open class AtomicIntegerArray : Serializable {
    private val array: Array<kotlin.concurrent.AtomicInt>

    actual constructor(length: Int) {
        array = Array(length) { kotlin.concurrent.AtomicInt(0) }
    }

    actual constructor(array: IntArray) {
        this.array = Array(array.size) { i -> kotlin.concurrent.AtomicInt(array[i]) }
    }

    private fun checkIndex(i: Int) {
        if (i < 0 || i >= array.size) throw IndexOutOfBoundsException("index $i, length ${array.size}")
    }

    actual fun length(): Int = array.size

    actual fun get(i: Int): Int {
        checkIndex(i)
        return array[i].value
    }

    actual fun set(i: Int, newValue: Int) {
        checkIndex(i)
        array[i].value = newValue
    }

    actual fun lazySet(i: Int, newValue: Int) {
        set(i, newValue)
    }

    actual fun getAndSet(i: Int, newValue: Int): Int {
        checkIndex(i)
        val cell = array[i]
        var current: Int
        do {
            current = cell.value
        } while (!cell.compareAndSet(current, newValue))
        return current
    }

    actual fun compareAndSet(i: Int, expect: Int, update: Int): Boolean {
        checkIndex(i)
        return array[i].compareAndSet(expect, update)
    }

    actual fun weakCompareAndSet(i: Int, expect: Int, update: Int): Boolean {
        return compareAndSet(i, expect, update)
    }

    actual fun getAndIncrement(i: Int): Int = getAndAdd(i, 1)

    actual fun getAndDecrement(i: Int): Int = getAndAdd(i, -1)

    actual fun getAndAdd(i: Int, delta: Int): Int {
        checkIndex(i)
        return array[i].addAndGet(delta) - delta
    }

    actual fun incrementAndGet(i: Int): Int = addAndGet(i, 1)

    actual fun decrementAndGet(i: Int): Int = addAndGet(i, -1)

    actual fun addAndGet(i: Int, delta: Int): Int {
        checkIndex(i)
        return array[i].addAndGet(delta)
    }

    actual fun getAndUpdate(i: Int, updateFunction: IntUnaryOperator): Int {
        checkIndex(i)
        var prev: Int
        var next: Int
        do {
            prev = get(i)
            next = updateFunction.applyAsInt(prev)
        } while (!compareAndSet(i, prev, next))
        return prev
    }

    actual fun updateAndGet(i: Int, updateFunction: IntUnaryOperator): Int {
        checkIndex(i)
        var prev: Int
        var next: Int
        do {
            prev = get(i)
            next = updateFunction.applyAsInt(prev)
        } while (!compareAndSet(i, prev, next))
        return next
    }

    actual fun getAndAccumulate(i: Int, x: Int, accumulatorFunction: IntBinaryOperator): Int {
        checkIndex(i)
        var prev: Int
        var next: Int
        do {
            prev = get(i)
            next = accumulatorFunction.applyAsInt(prev, x)
        } while (!compareAndSet(i, prev, next))
        return prev
    }

    actual fun accumulateAndGet(i: Int, x: Int, accumulatorFunction: IntBinaryOperator): Int {
        checkIndex(i)
        var prev: Int
        var next: Int
        do {
            prev = get(i)
            next = accumulatorFunction.applyAsInt(prev, x)
        } while (!compareAndSet(i, prev, next))
        return next
    }

    actual override fun toString(): String {
        val iMax = array.size - 1
        if (iMax == -1) return "[]"
        val b = StringBuilder()
        b.append('[')
        var i = 0
        while (true) {
            b.append(array[i].value)
            if (i == iMax) return b.append(']').toString()
            b.append(", ")
            i++
        }
    }
}
