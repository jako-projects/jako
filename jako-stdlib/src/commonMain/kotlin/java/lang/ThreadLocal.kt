@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

expect open class ThreadLocal<T> {
    constructor()
    open fun get(): T
    open fun set(value: T?)
    open fun remove()
    protected open fun initialValue(): T?

    companion object {
        fun <S> withInitial(supplier: () -> S): ThreadLocal<S>
    }
}
