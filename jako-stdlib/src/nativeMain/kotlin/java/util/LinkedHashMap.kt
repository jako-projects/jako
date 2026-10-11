package java.util

import java.io.Serializable
import java.lang.Cloneable

actual open class LinkedHashMap<K, V> : MutableMap<K, V>, Cloneable, Serializable {
    private val map: kotlin.collections.LinkedHashMap<K, V>

    actual constructor(initialCapacity: Int, loadFactor: Float, accessOrder: Boolean) {
        map = kotlin.collections.LinkedHashMap(initialCapacity, loadFactor)
    }

    actual constructor(initialCapacity: Int, loadFactor: Float) {
        map = kotlin.collections.LinkedHashMap(initialCapacity, loadFactor)
    }

    actual constructor(initialCapacity: Int) {
        map = kotlin.collections.LinkedHashMap(initialCapacity)
    }

    actual constructor() {
        map = kotlin.collections.LinkedHashMap()
    }

    actual constructor(m: Map<out K, V>) {
        map = kotlin.collections.LinkedHashMap(m)
    }

    actual override val size: Int get() = map.size
    actual override fun isEmpty(): Boolean = map.isEmpty()
    actual override fun containsKey(key: K): Boolean = map.containsKey(key)
    actual override fun containsValue(value: V): Boolean = map.containsValue(value)
    actual override fun get(key: K): V? = map[key]
    actual override fun put(key: K, value: V): V? = map.put(key, value)
    actual override fun remove(key: K): V? = map.remove(key)
    actual override fun putAll(from: Map<out K, V>) = map.putAll(from)
    actual override fun clear() = map.clear()
    actual override val keys: MutableSet<K> get() = map.keys
    actual override val values: MutableCollection<V> get() = map.values
    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = map.entries
}

