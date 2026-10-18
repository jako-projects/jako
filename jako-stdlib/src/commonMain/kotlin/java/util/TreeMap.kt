@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Cloneable

expect interface SortedMap<K, V> : MutableMap<K, V> {
    fun comparator(): Comparator<in K>?
    fun firstKey(): K
    fun lastKey(): K
}

/**
 * [Nullability Decision] Navigation methods (lower*, floor*, ceiling*, higher*, firstEntry, lastEntry, poll*)
 * return null when no matching key/entry exists in the map.
 */
expect interface NavigableMap<K, V> : SortedMap<K, V> {
    fun lowerEntry(key: K): MutableMap.MutableEntry<K, V>?
    fun lowerKey(key: K): K?
    fun floorEntry(key: K): MutableMap.MutableEntry<K, V>?
    fun floorKey(key: K): K?
    fun ceilingEntry(key: K): MutableMap.MutableEntry<K, V>?
    fun ceilingKey(key: K): K?
    fun higherEntry(key: K): MutableMap.MutableEntry<K, V>?
    fun higherKey(key: K): K?
    fun firstEntry(): MutableMap.MutableEntry<K, V>?
    fun lastEntry(): MutableMap.MutableEntry<K, V>?
    fun pollFirstEntry(): MutableMap.MutableEntry<K, V>?
    fun pollLastEntry(): MutableMap.MutableEntry<K, V>?
}

expect open class TreeMap<K, V> : AbstractMutableMap<K, V>, NavigableMap<K, V>, Cloneable, Serializable {
    constructor()
    constructor(comparator: Comparator<in K>?)
    constructor(m: Map<out K, V>)
    constructor(m: SortedMap<K, out V>)

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

    override fun put(key: K, value: V): V?
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
}

