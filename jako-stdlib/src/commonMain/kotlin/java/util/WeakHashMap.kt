@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

/**
 * [Nullability Decision] WeakHashMap permits null keys and values when K/V are nullable.
 */
expect open class WeakHashMap<K, V> : AbstractMutableMap<K, V>, MutableMap<K, V> {
    constructor(initialCapacity: Int, loadFactor: Float)
    constructor(initialCapacity: Int)
    constructor()
    constructor(m: Map<out K, V>)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun get(key: K): V?
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override fun put(key: K, value: V): V?
    override fun putAll(from: Map<out K, V>)
    override fun remove(key: K): V?
    override fun clear()
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
}
