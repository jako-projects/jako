@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Class
import java.lang.Cloneable

/**
 * [Nullability Decision] EnumMap does not permit null keys (K : Enum<K>), permits null values when V is nullable.
 */
expect open class EnumMap<K : Enum<K>, V> : AbstractMutableMap<K, V>, Serializable, Cloneable {
    constructor(keyType: Class<K>)
    constructor(m: EnumMap<K, out V>)
    constructor(m: Map<K, V>)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override fun get(key: K): V?
    override fun put(key: K, value: V): V?
    override fun remove(key: K): V?
    override fun putAll(from: Map<out K, V>)
    override fun clear()
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    open fun clone(): EnumMap<K, V>
}
