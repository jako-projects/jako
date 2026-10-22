package java.util.concurrent.atomic

import java.io.Serializable

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
