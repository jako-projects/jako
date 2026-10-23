package java.util

import java.io.Serializable
import java.lang.Cloneable

actual open class LinkedHashMap<K, V> : MutableMap<K, V>, Cloneable, Serializable {
    private val map: kotlin.collections.LinkedHashMap<K, V>
    private val accessOrder: Boolean

    actual constructor(initialCapacity: Int, loadFactor: Float, accessOrder: Boolean) {
        this.map = kotlin.collections.LinkedHashMap(initialCapacity, loadFactor)
        this.accessOrder = accessOrder
    }

    actual constructor(initialCapacity: Int, loadFactor: Float) : this(initialCapacity, loadFactor, false)

    actual constructor(initialCapacity: Int) {
        this.map = kotlin.collections.LinkedHashMap(initialCapacity)
        this.accessOrder = false
    }

    actual constructor() {
        this.map = kotlin.collections.LinkedHashMap()
        this.accessOrder = false
    }

    actual constructor(m: Map<out K, V>) {
        this.map = kotlin.collections.LinkedHashMap(m)
        this.accessOrder = false
    }

    actual override val size: Int get() = map.size
    actual override fun isEmpty(): Boolean = map.isEmpty()
    actual override fun containsKey(key: K): Boolean = map.containsKey(key)
    actual override fun containsValue(value: V): Boolean = map.containsValue(value)
    actual override fun get(key: K): V? {
        if (!accessOrder) return map[key]
        if (!map.containsKey(key)) return null
        val v = map.remove(key) as V
        map[key] = v
        return v
    }
    actual override fun put(key: K, value: V): V? {
        if (accessOrder && map.containsKey(key)) {
            val prev = map.remove(key)
            map[key] = value
            return prev
        }
        return map.put(key, value)
    }
    actual override fun remove(key: K): V? = map.remove(key)
    actual override fun putAll(from: Map<out K, V>) = map.putAll(from)
    actual override fun clear() = map.clear()
    actual override val keys: MutableSet<K> get() = map.keys
    actual override val values: MutableCollection<V> get() = map.values
    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = map.entries
    actual override fun equals(other: Any?): Boolean = map == other
    actual override fun hashCode(): Int = map.hashCode()
    actual override fun toString(): String = map.toString()
}
