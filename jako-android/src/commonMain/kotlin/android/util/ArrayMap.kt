@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

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
