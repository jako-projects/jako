@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang.ref

/**
 * [Nullability Decision] Reference.get() returns T? (null after clear() or GC, or always null for PhantomReference).
 */
expect abstract class Reference<T> {
    open fun get(): T?
    open fun clear()
    open fun isEnqueued(): Boolean
    open fun enqueue(): Boolean
}
