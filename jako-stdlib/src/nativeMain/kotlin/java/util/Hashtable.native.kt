package java.util

import java.io.Serializable
import java.lang.Cloneable

actual open class Hashtable<K, V> : Dictionary<K, V>, MutableMap<K, V>, Cloneable, Serializable {
    private val map: LinkedHashMap<K, V>

    actual constructor(initialCapacity: Int, loadFactor: Float) : super() {
        if (initialCapacity < 0) throw IllegalArgumentException("Illegal Capacity: $initialCapacity")
        if (loadFactor <= 0f || loadFactor.isNaN()) throw IllegalArgumentException("Illegal Load: $loadFactor")
        this.map = LinkedHashMap(initialCapacity, loadFactor)
    }

    actual constructor(initialCapacity: Int) : this(initialCapacity, 0.75f)

    actual constructor() : this(11, 0.75f)

    actual constructor(t: Map<out K, V>) : this(maxOf(2 * t.size, 11), 0.75f) {
        putAll(t)
    }

    actual override val size: Int
        get() = map.size

    actual override fun isEmpty(): Boolean = map.isEmpty()

    actual override fun keys(): Enumeration<K> = Collections.enumeration(map.keys.toList())

    actual override fun elements(): Enumeration<V> = Collections.enumeration(map.values.toList())

    actual open fun contains(value: Any?): Boolean {
        if (value == null) throw NullPointerException()
        @Suppress("UNCHECKED_CAST")
        return map.containsValue(value as V)
    }

    actual override fun containsValue(value: V): Boolean = contains(value)

    actual override fun containsKey(key: K): Boolean {
        if (key == null) throw NullPointerException()
        return map.containsKey(key)
    }

    actual override fun get(key: K): V? {
        if (key == null) throw NullPointerException()
        return map[key]
    }

    actual override fun put(key: K, value: V): V? {
        if (key == null || value == null) throw NullPointerException()
        return map.put(key, value)
    }

    actual override fun remove(key: K): V? {
        if (key == null) throw NullPointerException()
        return map.remove(key)
    }

    actual override fun putAll(from: Map<out K, V>) {
        for ((k, v) in from) {
            put(k, v)
        }
    }

    actual override fun clear() {
        map.clear()
    }

    actual open fun clone(): Any {
        val copy = Hashtable<K, V>()
        copy.map.putAll(this.map)
        return copy
    }

    actual override val keys: MutableSet<K>
        get() = map.keys

    actual override val values: MutableCollection<V>
        get() = map.values

    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = map.entries

    override fun equals(other: Any?): Boolean = map == other

    override fun hashCode(): Int = map.hashCode()

    override fun toString(): String = map.toString()
}
