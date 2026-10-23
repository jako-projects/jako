@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

expect open class InheritableThreadLocal<T> : ThreadLocal<T> {
    constructor()
    protected open fun childValue(parentValue: T?): T?
}
