package java.util.concurrent.atomic

import java.io.Serializable

actual class AtomicBoolean actual constructor(initialValue: Boolean) : Serializable {
    actual constructor() : this(false)

    private val delegate = AtomicInteger(if (initialValue) 1 else 0)

    actual fun get(): Boolean = delegate.get() != 0

    actual fun set(newValue: Boolean) {
        delegate.set(if (newValue) 1 else 0)
    }

    actual fun lazySet(newValue: Boolean) {
        delegate.lazySet(if (newValue) 1 else 0)
    }

    actual fun getAndSet(newValue: Boolean): Boolean {
        val newInt = if (newValue) 1 else 0
        var current: Int
        do {
            current = delegate.get()
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
