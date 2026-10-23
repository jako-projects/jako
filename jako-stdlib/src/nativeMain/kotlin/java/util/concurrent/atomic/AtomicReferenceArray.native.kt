package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.BinaryOperator
import java.util.function.UnaryOperator

actual open class AtomicReferenceArray<E> : Serializable {
    private val array: Array<kotlin.concurrent.AtomicReference<Any?>>

    actual constructor(length: Int) {
        array = Array(length) { kotlin.concurrent.AtomicReference<Any?>(null) }
    }

    actual constructor(array: Array<E>) {
        this.array = Array(array.size) { i -> kotlin.concurrent.AtomicReference<Any?>(array[i]) }
    }

    private fun checkIndex(i: Int) {
        if (i < 0 || i >= array.size) throw IndexOutOfBoundsException("index $i, length ${array.size}")
    }

    actual fun length(): Int = array.size

    @Suppress("UNCHECKED_CAST")
    actual fun get(i: Int): E? {
        checkIndex(i)
        return array[i].value as E?
    }

    actual fun set(i: Int, newValue: E?) {
        checkIndex(i)
        array[i].value = newValue
    }

    actual fun lazySet(i: Int, newValue: E?) {
        set(i, newValue)
    }

    @Suppress("UNCHECKED_CAST")
    actual fun getAndSet(i: Int, newValue: E?): E? {
        checkIndex(i)
        val cell = array[i]
        var current: Any?
        do {
            current = cell.value
        } while (!cell.compareAndSet(current, newValue))
        return current as E?
    }

    actual fun compareAndSet(i: Int, expect: E?, update: E?): Boolean {
        checkIndex(i)
        return array[i].compareAndSet(expect as Any?, update as Any?)
    }

    actual fun weakCompareAndSet(i: Int, expect: E?, update: E?): Boolean {
        return compareAndSet(i, expect, update)
    }

    actual fun getAndUpdate(i: Int, updateFunction: UnaryOperator<E?>): E? {
        checkIndex(i)
        var prev: E?
        var next: E?
        do {
            prev = get(i)
            next = updateFunction.apply(prev)
        } while (!compareAndSet(i, prev, next))
        return prev
    }

    actual fun updateAndGet(i: Int, updateFunction: UnaryOperator<E?>): E? {
        checkIndex(i)
        var prev: E?
        var next: E?
        do {
            prev = get(i)
            next = updateFunction.apply(prev)
        } while (!compareAndSet(i, prev, next))
        return next
    }

    actual fun getAndAccumulate(i: Int, x: E?, accumulatorFunction: BinaryOperator<E?>): E? {
        checkIndex(i)
        var prev: E?
        var next: E?
        do {
            prev = get(i)
            next = accumulatorFunction.apply(prev, x)
        } while (!compareAndSet(i, prev, next))
        return prev
    }

    actual fun accumulateAndGet(i: Int, x: E?, accumulatorFunction: BinaryOperator<E?>): E? {
        checkIndex(i)
        var prev: E?
        var next: E?
        do {
            prev = get(i)
            next = accumulatorFunction.apply(prev, x)
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
