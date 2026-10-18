package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.BinaryOperator
import java.util.function.IntBinaryOperator
import java.util.function.IntUnaryOperator
import java.util.function.LongBinaryOperator
import java.util.function.LongUnaryOperator
import java.util.function.UnaryOperator

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

actual class AtomicBoolean actual constructor(initialValue: Boolean) : Serializable {
    actual constructor() : this(false)

    private val delegate = kotlin.concurrent.AtomicInt(if (initialValue) 1 else 0)

    actual fun get(): Boolean = delegate.value != 0

    actual fun set(newValue: Boolean) {
        delegate.value = if (newValue) 1 else 0
    }

    actual fun lazySet(newValue: Boolean) {
        delegate.value = if (newValue) 1 else 0
    }

    actual fun getAndSet(newValue: Boolean): Boolean {
        val newInt = if (newValue) 1 else 0
        var current: Int
        do {
            current = delegate.value
        } while (!delegate.compareAndSet(current, newInt))
        return current != 0
    }

    actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean {
        val expectInt = if (expect) 1 else 0
        val updateInt = if (update) 1 else 0
        return delegate.compareAndSet(expectInt, updateInt)
    }

    actual fun weakCompareAndSet(expect: Boolean, update: Boolean): Boolean {
        return compareAndSet(expect, update)
    }

    override fun toString(): String = get().toString()
}

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

actual open class LongAdder actual constructor() : Number(), Serializable {
    private val base = kotlin.concurrent.AtomicLong(0L)

    actual open fun add(x: Long) {
        base.addAndGet(x)
    }

    actual open fun increment() {
        base.addAndGet(1L)
    }

    actual open fun decrement() {
        base.addAndGet(-1L)
    }

    actual open fun sum(): Long = base.value

    actual open fun reset() {
        base.value = 0L
    }

    actual open fun sumThenReset(): Long {
        while (true) {
            val current = base.value
            if (base.compareAndSet(current, 0L)) {
                return current
            }
        }
    }

    override fun toString(): String = sum().toString()

    actual override fun toByte(): Byte = sum().toByte()
    actual override fun toShort(): Short = sum().toShort()
    actual override fun toInt(): Int = sum().toInt()
    actual override fun toLong(): Long = sum()
    actual override fun toFloat(): Float = sum().toFloat()
    actual override fun toDouble(): Double = sum().toDouble()
}

actual open class DoubleAdder actual constructor() : Number(), Serializable {
    private val baseBits = kotlin.concurrent.AtomicLong(0.0.toRawBits())

    actual open fun add(x: Double) {
        while (true) {
            val currentBits = baseBits.value
            val nextVal = Double.fromBits(currentBits) + x
            if (baseBits.compareAndSet(currentBits, nextVal.toRawBits())) {
                return
            }
        }
    }

    actual open fun sum(): Double = Double.fromBits(baseBits.value)

    actual open fun reset() {
        baseBits.value = 0.0.toRawBits()
    }

    actual open fun sumThenReset(): Double {
        while (true) {
            val currentBits = baseBits.value
            if (baseBits.compareAndSet(currentBits, 0.0.toRawBits())) {
                return Double.fromBits(currentBits)
            }
        }
    }

    override fun toString(): String = sum().toString()

    actual override fun toByte(): Byte = sum().toInt().toByte()
    actual override fun toShort(): Short = sum().toInt().toShort()
    actual override fun toInt(): Int = sum().toInt()
    actual override fun toLong(): Long = sum().toLong()
    actual override fun toFloat(): Float = sum().toFloat()
    actual override fun toDouble(): Double = sum()
}

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

actual open class AtomicLongArray : Serializable {
    private val array: Array<kotlin.concurrent.AtomicLong>

    actual constructor(length: Int) {
        array = Array(length) { kotlin.concurrent.AtomicLong(0L) }
    }

    actual constructor(array: LongArray) {
        this.array = Array(array.size) { i -> kotlin.concurrent.AtomicLong(array[i]) }
    }

    private fun checkIndex(i: Int) {
        if (i < 0 || i >= array.size) throw IndexOutOfBoundsException("index $i, length ${array.size}")
    }

    actual fun length(): Int = array.size

    actual fun get(i: Int): Long {
        checkIndex(i)
        return array[i].value
    }

    actual fun set(i: Int, newValue: Long) {
        checkIndex(i)
        array[i].value = newValue
    }

    actual fun lazySet(i: Int, newValue: Long) {
        set(i, newValue)
    }

    actual fun getAndSet(i: Int, newValue: Long): Long {
        checkIndex(i)
        val cell = array[i]
        var current: Long
        do {
            current = cell.value
        } while (!cell.compareAndSet(current, newValue))
        return current
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
        return array[i].addAndGet(delta) - delta
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
            b.append(array[i].value)
            if (i == iMax) return b.append(']').toString()
            b.append(", ")
            i++
        }
    }
}

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

actual open class AtomicStampedReference<V> actual constructor(
    initialRef: V?,
    initialStamp: Int
) {
    private class Pair<T>(val reference: T?, val stamp: Int)

    private val pairRef = kotlin.concurrent.AtomicReference(Pair(initialRef, initialStamp))

    actual open fun getReference(): V? = pairRef.value.reference

    actual open fun getStamp(): Int = pairRef.value.stamp

    actual open val reference: V? get() = getReference()

    actual open val stamp: Int get() = getStamp()

    actual open fun get(stampHolder: IntArray): V? {
        val current = pairRef.value
        stampHolder[0] = current.stamp
        return current.reference
    }

    actual open fun weakCompareAndSet(
        expectedReference: V?,
        newReference: V?,
        expectedStamp: Int,
        newStamp: Int
    ): Boolean = compareAndSet(expectedReference, newReference, expectedStamp, newStamp)

    actual open fun compareAndSet(
        expectedReference: V?,
        newReference: V?,
        expectedStamp: Int,
        newStamp: Int
    ): Boolean {
        val current = pairRef.value
        return expectedReference === current.reference &&
            expectedStamp == current.stamp &&
            ((newReference === current.reference && newStamp == current.stamp) ||
                pairRef.compareAndSet(current, Pair(newReference, newStamp)))
    }

    actual open fun set(newReference: V?, newStamp: Int) {
        val current = pairRef.value
        if (newReference !== current.reference || newStamp != current.stamp) {
            pairRef.value = Pair(newReference, newStamp)
        }
    }

    actual open fun attemptStamp(expectedReference: V?, newStamp: Int): Boolean {
        val current = pairRef.value
        return expectedReference === current.reference &&
            (newStamp == current.stamp ||
                pairRef.compareAndSet(current, Pair(expectedReference, newStamp)))
    }
}

actual open class AtomicMarkableReference<V> actual constructor(
    initialRef: V?,
    initialMark: Boolean
) {
    private class Pair<T>(val reference: T?, val mark: Boolean)

    private val pairRef = kotlin.concurrent.AtomicReference(Pair(initialRef, initialMark))

    actual open fun getReference(): V? = pairRef.value.reference

    actual open fun isMarked(): Boolean = pairRef.value.mark

    actual open val reference: V? get() = getReference()

    actual open val isMarked: Boolean get() = isMarked()

    actual open fun get(markHolder: BooleanArray): V? {
        val current = pairRef.value
        markHolder[0] = current.mark
        return current.reference
    }

    actual open fun weakCompareAndSet(
        expectedReference: V?,
        newReference: V?,
        expectedMark: Boolean,
        newMark: Boolean
    ): Boolean = compareAndSet(expectedReference, newReference, expectedMark, newMark)

    actual open fun compareAndSet(
        expectedReference: V?,
        newReference: V?,
        expectedMark: Boolean,
        newMark: Boolean
    ): Boolean {
        val current = pairRef.value
        return expectedReference === current.reference &&
            expectedMark == current.mark &&
            ((newReference === current.reference && newMark == current.mark) ||
                pairRef.compareAndSet(current, Pair(newReference, newMark)))
    }

    actual open fun set(newReference: V?, newMark: Boolean) {
        val current = pairRef.value
        if (newReference !== current.reference || newMark != current.mark) {
            pairRef.value = Pair(newReference, newMark)
        }
    }

    actual open fun attemptMark(expectedReference: V?, newMark: Boolean): Boolean {
        val current = pairRef.value
        return expectedReference === current.reference &&
            (newMark == current.mark ||
                pairRef.compareAndSet(current, Pair(expectedReference, newMark)))
    }
}

