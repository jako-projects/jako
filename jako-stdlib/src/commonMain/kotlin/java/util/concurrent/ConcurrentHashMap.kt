@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable

expect open class ConcurrentHashMap<K, V>() : MutableMap<K, V>, Serializable {
    override fun put(key: K, value: V): V?
    override operator fun get(key: K): V?
    override fun remove(key: K): V?
    override fun clear()
    override val size: Int
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    override fun isEmpty(): Boolean
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override fun putAll(from: Map<out K, V>)
    open fun keySet(): MutableSet<K>
}
