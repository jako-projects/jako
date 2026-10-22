package java.util.concurrent

import java.io.Serializable
import kotlin.concurrent.withLock

actual open class ConcurrentHashMap<K, V> actual constructor() : MutableMap<K, V>, Serializable {
    private val map = HashMap<K, V>()
    actual override fun put(key: K, value: V): V? = map.put(key, value)
    actual override operator fun get(key: K): V? = map.get(key)
    actual override fun remove(key: K): V? = map.remove(key)
    actual override fun clear() = map.clear()
    actual override val size: Int get() = map.size
    actual override val keys: MutableSet<K> get() = map.keys
    actual override val values: MutableCollection<V> get() = map.values
    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = map.entries
    actual override fun isEmpty(): Boolean = map.isEmpty()
    actual override fun containsKey(key: K): Boolean = map.containsKey(key)
    actual override fun containsValue(value: V): Boolean = map.containsValue(value)
    actual override fun putAll(from: Map<out K, V>) = map.putAll(from)
    actual open fun keySet(): MutableSet<K> = map.keys
}
