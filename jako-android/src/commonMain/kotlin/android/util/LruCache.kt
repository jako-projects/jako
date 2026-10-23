@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

/**
 * A cache that holds strong references to a limited number of values. Each time
 * a value is accessed, it is moved to the head of a queue. When a value is
 * added to a full cache, the value at the end of that queue is evicted.
 */
expect open class LruCache<K, V>(maxSize: Int) {
    open fun resize(maxSize: Int)
    operator fun get(key: K): V?
    fun put(key: K, value: V): V?
    open fun trimToSize(maxSize: Int)
    fun remove(key: K): V?
    protected open fun entryRemoved(evicted: Boolean, key: K, oldValue: V, newValue: V?)
    protected open fun create(key: K): V?
    protected open fun sizeOf(key: K, value: V): Int
    fun evictAll()
    fun size(): Int
    fun maxSize(): Int
    fun hitCount(): Int
    fun missCount(): Int
    fun createCount(): Int
    fun putCount(): Int
    fun evictionCount(): Int
    fun snapshot(): MutableMap<K, V>
    override fun toString(): String
}
