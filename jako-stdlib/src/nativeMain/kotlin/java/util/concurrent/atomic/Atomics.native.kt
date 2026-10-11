package java.util.concurrent.atomic

import java.io.Serializable

actual class AtomicInteger actual constructor(initialValue: Int) : Number(), Serializable {
    actual constructor() : this(0)

    private val delegate = kotlin.concurrent.AtomicInt(initialValue)

    actual fun get(): Int = delegate.value

    actual fun set(newValue: Int) {
        delegate.value = newValue
    }

    fun lazySet(newValue: Int) {
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

    actual fun getAndIncrement(): Int = delegate.addAndGet(1) - 1

    actual fun getAndDecrement(): Int = delegate.addAndGet(-1) + 1

    actual fun getAndAdd(delta: Int): Int = delegate.addAndGet(delta) - delta

    actual fun incrementAndGet(): Int = delegate.addAndGet(1)

    actual fun decrementAndGet(): Int = delegate.addAndGet(-1)

    actual fun addAndGet(delta: Int): Int = delegate.addAndGet(delta)

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

    fun lazySet(newValue: Long) {
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

    actual fun getAndIncrement(): Long = delegate.addAndGet(1L) - 1L

    actual fun getAndDecrement(): Long = delegate.addAndGet(-1L) + 1L

    actual fun getAndAdd(delta: Long): Long = delegate.addAndGet(delta) - delta

    actual fun incrementAndGet(): Long = delegate.addAndGet(1L)

    actual fun decrementAndGet(): Long = delegate.addAndGet(-1L)

    actual fun addAndGet(delta: Long): Long = delegate.addAndGet(delta)

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

    fun lazySet(newValue: Boolean) {
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

    fun lazySet(newValue: V?) {
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
}
