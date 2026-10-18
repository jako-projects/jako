@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

import java.lang.Cloneable

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

/**
 * SparseArrays map integers to Objects. Unlike a normal array of Objects,
 * there can be gaps in the indices.
 */
expect open class SparseArray<E> : Cloneable {
    constructor()
    constructor(initialCapacity: Int)

    open fun clone(): SparseArray<E>
    open operator fun get(key: Int): E?
    open fun get(key: Int, valueIfKeyNotFound: E): E
    open fun delete(key: Int)
    open fun remove(key: Int)
    open fun removeAt(index: Int)
    open fun removeAtRange(index: Int, size: Int)
    open fun put(key: Int, value: E)
    open fun size(): Int
    open fun keyAt(index: Int): Int
    open fun valueAt(index: Int): E
    open fun setValueAt(index: Int, value: E)
    open fun indexOfKey(key: Int): Int
    open fun indexOfValue(value: E): Int
    open fun clear()
    open fun append(key: Int, value: E)
    override fun toString(): String
}

/**
 * SparseBooleanArrays map integers to booleans.
 */
expect open class SparseBooleanArray : Cloneable {
    constructor()
    constructor(initialCapacity: Int)

    open fun clone(): SparseBooleanArray
    open operator fun get(key: Int): Boolean
    open fun get(key: Int, valueIfKeyNotFound: Boolean): Boolean
    open fun delete(key: Int)
    open fun removeAt(index: Int)
    open fun put(key: Int, value: Boolean)
    open fun size(): Int
    open fun keyAt(index: Int): Int
    open fun valueAt(index: Int): Boolean
    open fun setValueAt(index: Int, value: Boolean)
    open fun indexOfKey(key: Int): Int
    open fun indexOfValue(value: Boolean): Int
    open fun clear()
    open fun append(key: Int, value: Boolean)
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
}

/**
 * SparseIntArrays map integers to integers.
 */
expect open class SparseIntArray : Cloneable {
    constructor()
    constructor(initialCapacity: Int)

    open fun clone(): SparseIntArray
    open operator fun get(key: Int): Int
    open fun get(key: Int, valueIfKeyNotFound: Int): Int
    open fun delete(key: Int)
    open fun removeAt(index: Int)
    open fun put(key: Int, value: Int)
    open fun size(): Int
    open fun keyAt(index: Int): Int
    open fun valueAt(index: Int): Int
    open fun setValueAt(index: Int, value: Int)
    open fun indexOfKey(key: Int): Int
    open fun indexOfValue(value: Int): Int
    open fun clear()
    open fun append(key: Int, value: Int)
    open fun copyKeys(): IntArray?
    override fun toString(): String
}

/**
 * SparseLongArrays map integers to Long.
 */
expect open class SparseLongArray : Cloneable {
    constructor()
    constructor(initialCapacity: Int)

    open fun clone(): SparseLongArray
    open operator fun get(key: Int): Long
    open fun get(key: Int, valueIfKeyNotFound: Long): Long
    open fun delete(key: Int)
    open fun removeAt(index: Int)
    open fun put(key: Int, value: Long)
    open fun size(): Int
    open fun keyAt(index: Int): Int
    open fun valueAt(index: Int): Long
    open fun indexOfKey(key: Int): Int
    open fun indexOfValue(value: Long): Int
    open fun clear()
    open fun append(key: Int, value: Long)
    override fun toString(): String
}

/**
 * SparseArray mapping longs to Objects.
 */
expect open class LongSparseArray<E> : Cloneable {
    constructor()
    constructor(initialCapacity: Int)

    open fun clone(): LongSparseArray<E>
    open operator fun get(key: Long): E?
    open fun get(key: Long, valueIfKeyNotFound: E): E
    open fun delete(key: Long)
    open fun remove(key: Long)
    open fun removeAt(index: Int)
    open fun put(key: Long, value: E)
    open fun size(): Int
    open fun keyAt(index: Int): Long
    open fun valueAt(index: Int): E
    open fun setValueAt(index: Int, value: E)
    open fun indexOfKey(key: Long): Int
    open fun indexOfValue(value: E): Int
    open fun clear()
    open fun append(key: Long, value: E)
    override fun toString(): String
}

/**
 * ArrayMap is a generic key->value mapping data structure that is
 * designed to be more memory efficient than a traditional HashMap.
 */
expect final class ArrayMap<K, V> : MutableMap<K, V> {
    constructor()
    constructor(capacity: Int)
    constructor(map: ArrayMap<K, V>?)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override fun get(key: K): V?
    override fun put(key: K, value: V): V?
    override fun remove(key: K): V?
    override fun putAll(from: Map<out K, V>)
    override fun clear()
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>

    fun ensureCapacity(minimumCapacity: Int)
    fun indexOfKey(key: Any?): Int
    fun indexOfValue(value: Any?): Int
    fun keyAt(index: Int): K
    fun valueAt(index: Int): V
    fun setValueAt(index: Int, value: V): V
    fun removeAt(index: Int): V
    fun putAll(array: ArrayMap<out K, out V>)
    fun containsAll(collection: Collection<*>): Boolean
    fun removeAll(collection: Collection<*>): Boolean
    fun retainAll(collection: Collection<*>): Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
}

/**
 * ArraySet is a generic set data structure that is designed to be more memory efficient
 * than a traditional HashSet.
 */
expect final class ArraySet<E> : MutableCollection<E>, MutableSet<E> {
    constructor()
    constructor(capacity: Int)
    constructor(set: ArraySet<E>?)
    constructor(set: Collection<E>?)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: E): Boolean
    override fun iterator(): MutableIterator<E>
    override fun add(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun containsAll(elements: Collection<E>): Boolean
    override fun addAll(elements: Collection<E>): Boolean
    override fun retainAll(elements: Collection<E>): Boolean
    override fun removeAll(elements: Collection<E>): Boolean
    override fun clear()

    fun ensureCapacity(minimumCapacity: Int)
    fun valueAt(index: Int): E
    fun removeAt(index: Int): E
    fun indexOf(key: Any?): Int
    fun addAll(array: ArraySet<out E>)
    fun removeAll(array: ArraySet<out E>): Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
}
