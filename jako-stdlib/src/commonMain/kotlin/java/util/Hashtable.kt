@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Cloneable

expect open class Hashtable<K, V> : Dictionary<K, V>, MutableMap<K, V>, Cloneable, Serializable {
    constructor(initialCapacity: Int, loadFactor: Float)
    constructor(initialCapacity: Int)
    constructor()
    constructor(t: Map<out K, V>)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun keys(): Enumeration<K>
    override fun elements(): Enumeration<V>
    open fun contains(value: Any?): Boolean
    override fun containsValue(value: V): Boolean
    override fun containsKey(key: K): Boolean
    override fun get(key: K): V?
    override fun put(key: K, value: V): V?
    override fun remove(key: K): V?
    override fun putAll(from: Map<out K, V>)
    override fun clear()
    open fun clone(): Any
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
}
