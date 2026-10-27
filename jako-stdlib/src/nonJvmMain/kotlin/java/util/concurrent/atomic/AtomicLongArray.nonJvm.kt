package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.LongBinaryOperator
import java.util.function.LongUnaryOperator

actual open class AtomicLongArray : Serializable {
    private val array: Array<AtomicLong>

    actual constructor(length: Int) {
        array = Array(length) { AtomicLong(0L) }
    }

    actual constructor(array: LongArray) {
        this.array = Array(array.size) { i -> AtomicLong(array[i]) }
    }

    private fun checkIndex(i: Int) {
        if (i < 0 || i >= array.size) throw IndexOutOfBoundsException("index $i, length ${array.size}")
    }

    actual fun length(): Int = array.size

    actual fun get(i: Int): Long {
        checkIndex(i)
        return array[i].get()
    }

    actual fun set(i: Int, newValue: Long) {
        checkIndex(i)
        array[i].set(newValue)
    }

    actual fun lazySet(i: Int, newValue: Long) {
        set(i, newValue)
    }

    actual fun getAndSet(i: Int, newValue: Long): Long {
        checkIndex(i)
        return array[i].getAndSet(newValue)
    }

    actual fun compareAndSet(i: Int, expect: Long, update: Long): Boolean {
        checkIndex(i)
        return array[i].compareAndSet(expect, update)
    }

    actual fun weakCompareAndSet(i: Int, expect: Long, update: Long): Boolean {
        return compareAndSet(i, expect, update)
    }

    actual fun getAndIncrement(i: Int): Long = getAndAdd(i, 1L)

    actual fun getAndDecrement(i: Int): Long = getAndAdd(i, -1L)

    actual fun getAndAdd(i: Int, delta: Long): Long {
        checkIndex(i)
        return array[i].getAndAdd(delta)
    }

    actual fun incrementAndGet(i: Int): Long = addAndGet(i, 1L)

    actual fun decrementAndGet(i: Int): Long = addAndGet(i, -1L)

    actual fun addAndGet(i: Int, delta: Long): Long {
        checkIndex(i)
        return array[i].addAndGet(delta)
    }

    actual fun getAndUpdate(i: Int, updateFunction: LongUnaryOperator): Long {
        checkIndex(i)
        var prev: Long
        var next: Long
        do {
            prev = get(i)
            next = updateFunction.applyAsLong(prev)
        } while (!compareAndSet(i, prev, next))
        return prev
    }

    actual fun updateAndGet(i: Int, updateFunction: LongUnaryOperator): Long {
        checkIndex(i)
        var prev: Long
        var next: Long
        do {
            prev = get(i)
            next = updateFunction.applyAsLong(prev)
        } while (!compareAndSet(i, prev, next))
        return next
    }

    actual fun getAndAccumulate(i: Int, x: Long, accumulatorFunction: LongBinaryOperator): Long {
        checkIndex(i)
        var prev: Long
        var next: Long
        do {
            prev = get(i)
            next = accumulatorFunction.applyAsLong(prev, x)
        } while (!compareAndSet(i, prev, next))
        return prev
    }

    actual fun accumulateAndGet(i: Int, x: Long, accumulatorFunction: LongBinaryOperator): Long {
        checkIndex(i)
        var prev: Long
        var next: Long
        do {
            prev = get(i)
            next = accumulatorFunction.applyAsLong(prev, x)
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
            b.append(array[i].get())
            if (i == iMax) return b.append(']').toString()
            b.append(", ")
            i++
        }
    }
}
