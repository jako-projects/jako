package java.lang

actual open class ThreadLocal<T> actual constructor() {
    actual protected open fun initialValue(): T? = null

    @Suppress("UNCHECKED_CAST")
    actual open fun get(): T {
        val map = Thread.currentThread().threadLocalValues
        if (map.containsKey(this)) {
            return map[this] as T
        }
        val initial = initialValue()
        map[this] = initial
        return initial as T
    }

    actual open fun set(value: T?) {
        Thread.currentThread().threadLocalValues[this] = value
    }

    actual open fun remove() {
        Thread.currentThread().threadLocalValues.remove(this)
    }

    internal open fun copyForChild(parentValue: Any?): Any? = parentValue

    actual companion object {
        actual fun <S> withInitial(supplier: () -> S): ThreadLocal<S> {
            return object : ThreadLocal<S>() {
                override fun initialValue(): S = supplier()
            }
        }
    }
}
