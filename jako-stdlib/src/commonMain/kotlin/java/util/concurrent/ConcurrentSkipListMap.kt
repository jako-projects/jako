@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable
import java.lang.Cloneable
import java.util.NavigableMap

/**
 * [Nullability Decision] ConcurrentSkipListMap does not permit null keys or values.
 */
expect open class ConcurrentSkipListMap<K, V> : AbstractMutableMap<K, V>, NavigableMap<K, V>, Cloneable, Serializable {
    constructor()
    constructor(comparator: Comparator<in K>?)
    constructor(m: Map<out K, V>)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override operator fun get(key: K): V?
    override fun put(key: K, value: V): V?
    override fun remove(key: K): V?
    override fun clear()
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>

    open fun putIfAbsent(key: K, value: V): V?
    open fun remove(key: Any?, value: Any?): Boolean
    open fun replace(key: K, oldValue: V, newValue: V): Boolean
    open fun replace(key: K, value: V): V?
    override fun comparator(): Comparator<in K>?
    override fun firstKey(): K
    override fun lastKey(): K
    override fun lowerEntry(key: K): MutableMap.MutableEntry<K, V>?
    override fun lowerKey(key: K): K?
    override fun floorEntry(key: K): MutableMap.MutableEntry<K, V>?
    override fun floorKey(key: K): K?
    override fun ceilingEntry(key: K): MutableMap.MutableEntry<K, V>?
    override fun ceilingKey(key: K): K?
    override fun higherEntry(key: K): MutableMap.MutableEntry<K, V>?
    override fun higherKey(key: K): K?
    override fun firstEntry(): MutableMap.MutableEntry<K, V>?
    override fun lastEntry(): MutableMap.MutableEntry<K, V>?
    override fun pollFirstEntry(): MutableMap.MutableEntry<K, V>?
    override fun pollLastEntry(): MutableMap.MutableEntry<K, V>?
    open fun keySet(): MutableSet<K>
}
