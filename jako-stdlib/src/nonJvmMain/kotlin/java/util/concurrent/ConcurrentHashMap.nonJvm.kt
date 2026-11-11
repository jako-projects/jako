package java.util.concurrent

import java.io.Serializable
import java.util.concurrent.locks.ReentrantLock
import java.util.function.BiFunction
import java.util.function.Function

actual open class ConcurrentHashMap<K, V> : MutableMap<K, V>, Serializable {
    private val lock = ReentrantLock()
    private val map: HashMap<K, V>

    actual constructor() {
        this.map = HashMap()
    }

    actual constructor(initialCapacity: Int) {
        if (initialCapacity < 0) throw IllegalArgumentException("Negative initialCapacity: $initialCapacity")
        this.map = HashMap(initialCapacity)
    }

    actual constructor(m: Map<out K, V>) {
        this.map = HashMap(maxOf(16, m.size))
        putAll(m)
    }

    actual constructor(initialCapacity: Int, loadFactor: Float) : this(initialCapacity, loadFactor, 1)

    actual constructor(initialCapacity: Int, loadFactor: Float, concurrencyLevel: Int) {
        if (initialCapacity < 0 || loadFactor <= 0.0f || loadFactor.isNaN() || concurrencyLevel <= 0) {
            throw IllegalArgumentException()
        }
        this.map = HashMap(initialCapacity, loadFactor)
    }

    private inline fun <R> sync(block: () -> R): R {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }

    private fun checkKey(key: Any?) {
        if (key == null) throw NullPointerException("ConcurrentHashMap does not permit null keys")
    }

    private fun checkKeyAndValue(key: Any?, value: Any?) {
        if (key == null || value == null) throw NullPointerException("ConcurrentHashMap does not permit null keys or values")
    }

    actual override fun put(key: K, value: V): V? {
        checkKeyAndValue(key, value)
        return sync { map.put(key, value) }
    }

    actual override operator fun get(key: K): V? {
        checkKey(key)
        return sync { map[key] }
    }

    actual override fun remove(key: K): V? {
        checkKey(key)
        return sync { map.remove(key) }
    }

    actual override fun clear() = sync { map.clear() }

    actual override val size: Int get() = sync { map.size }
    actual override val keys: MutableSet<K> get() = map.keys
    actual override val values: MutableCollection<V> get() = map.values
    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = map.entries

    actual override fun isEmpty(): Boolean = sync { map.isEmpty() }

    actual override fun containsKey(key: K): Boolean {
        checkKey(key)
        return sync { map.containsKey(key) }
    }

    actual override fun containsValue(value: V): Boolean {
        if (value == null) throw NullPointerException("ConcurrentHashMap does not permit null values")
        return sync { map.containsValue(value) }
    }

    actual override fun putAll(from: Map<out K, V>) {
        sync {
            for ((k, v) in from) {
                checkKeyAndValue(k, v)
                map[k] = v
            }
        }
    }

    actual open fun keySet(): MutableSet<K> = map.keys

    actual open fun putIfAbsent(key: K, value: V): V? {
        checkKeyAndValue(key, value)
        return sync {
            val existing = map[key]
            if (existing == null) {
                map[key] = value
                null
            } else {
                existing
            }
        }
    }

    actual open fun remove(key: Any?, value: Any?): Boolean {
        checkKey(key)
        if (value == null) return false
        return sync {
            @Suppress("UNCHECKED_CAST")
            val k = key as K
            val cur = map[k]
            if (cur != null && cur == value) {
                map.remove(k)
                true
            } else {
                false
            }
        }
    }

    actual open fun replace(key: K, oldValue: V, newValue: V): Boolean {
        checkKeyAndValue(key, oldValue)
        if (newValue == null) throw NullPointerException("ConcurrentHashMap does not permit null values")
        return sync {
            val cur = map[key]
            if (cur != null && cur == oldValue) {
                map[key] = newValue
                true
            } else {
                false
            }
        }
    }

    actual open fun replace(key: K, value: V): V? {
        checkKeyAndValue(key, value)
        return sync {
            if (map.containsKey(key)) {
                map.put(key, value)
            } else {
                null
            }
        }
    }

    actual open fun getOrDefault(key: K, defaultValue: V): V {
        checkKey(key)
        return sync { map[key] ?: defaultValue }
    }

    actual open fun computeIfAbsent(key: K, mappingFunction: Function<in K, out V?>): V? {
        checkKey(key)
        return sync {
            val existing = map[key]
            if (existing != null) {
                existing
            } else {
                val newValue = mappingFunction.apply(key)
                if (newValue != null) {
                    map[key] = newValue
                }
                newValue
            }
        }
    }

    actual open fun computeIfPresent(key: K, remappingFunction: BiFunction<in K, in V, out V?>): V? {
        checkKey(key)
        return sync {
            val oldValue = map[key] ?: return@sync null
            val newValue = remappingFunction.apply(key, oldValue)
            if (newValue != null) {
                map[key] = newValue
                newValue
            } else {
                map.remove(key)
                null
            }
        }
    }

    actual open fun compute(key: K, remappingFunction: BiFunction<in K, in V?, out V?>): V? {
        checkKey(key)
        return sync {
            val oldValue = map[key]
            val newValue = remappingFunction.apply(key, oldValue)
            if (newValue != null) {
                map[key] = newValue
                newValue
            } else {
                if (oldValue != null) {
                    map.remove(key)
                }
                null
            }
        }
    }

    actual open fun merge(key: K, value: V, remappingFunction: BiFunction<in V, in V, out V?>): V? {
        checkKeyAndValue(key, value)
        return sync {
            val oldValue = map[key]
            val newValue = if (oldValue == null) value else remappingFunction.apply(oldValue, value)
            if (newValue != null) {
                map[key] = newValue
                newValue
            } else {
                map.remove(key)
                null
            }
        }
    }

    override fun equals(other: Any?): Boolean = sync { map == other }
    override fun hashCode(): Int = sync { map.hashCode() }
    override fun toString(): String = sync { map.toString() }
}
