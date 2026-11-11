@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable
import java.util.function.BiFunction
import java.util.function.Function

expect open class ConcurrentHashMap<K, V> : MutableMap<K, V>, Serializable {
    constructor()
    constructor(initialCapacity: Int)
    constructor(m: Map<out K, V>)
    constructor(initialCapacity: Int, loadFactor: Float)
    constructor(initialCapacity: Int, loadFactor: Float, concurrencyLevel: Int)

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

    open fun putIfAbsent(key: K, value: V): V?
    open fun remove(key: Any?, value: Any?): Boolean
    open fun replace(key: K, oldValue: V, newValue: V): Boolean
    open fun replace(key: K, value: V): V?
    open fun getOrDefault(key: K, defaultValue: V): V
    open fun computeIfAbsent(key: K, mappingFunction: Function<in K, out V?>): V?
    open fun computeIfPresent(key: K, remappingFunction: BiFunction<in K, in V, out V?>): V?
    open fun compute(key: K, remappingFunction: BiFunction<in K, in V?, out V?>): V?
    open fun merge(key: K, value: V, remappingFunction: BiFunction<in V, in V, out V?>): V?
}
