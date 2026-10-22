@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang.ref

/**
 * [Nullability Decision] ReferenceQueue.poll() and remove(timeout) return Reference<out T>? (null when empty/timed out).
 */
expect open class ReferenceQueue<T>() {
    open fun poll(): Reference<out T>?
    open fun remove(timeout: Long): Reference<out T>?
    open fun remove(): Reference<out T>?
}
