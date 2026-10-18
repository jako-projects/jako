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

/**
 * [Nullability Decision] ReferenceQueue.poll() and remove(timeout) return Reference<out T>? (null when empty/timed out).
 */
expect open class ReferenceQueue<T>() {
    open fun poll(): Reference<out T>?
    open fun remove(timeout: Long): Reference<out T>?
    open fun remove(): Reference<out T>?
}

expect open class WeakReference<T> : Reference<T> {
    constructor(referent: T?)
    constructor(referent: T?, q: ReferenceQueue<in T>?)
}

expect open class SoftReference<T> : Reference<T> {
    constructor(referent: T?)
    constructor(referent: T?, q: ReferenceQueue<in T>?)
    override fun get(): T?
}

expect open class PhantomReference<T> : Reference<T> {
    constructor(referent: T?, q: ReferenceQueue<in T>?)
    override fun get(): T?
}

