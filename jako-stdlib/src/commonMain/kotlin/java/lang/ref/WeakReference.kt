@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang.ref

expect abstract class Reference<T> {
    open fun get(): T?
    open fun clear()
}

expect open class WeakReference<T>(referent: T?) : Reference<T>
