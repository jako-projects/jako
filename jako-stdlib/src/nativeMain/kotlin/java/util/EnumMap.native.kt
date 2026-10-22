package java.util

import java.io.Serializable
import java.lang.Class
import java.lang.Cloneable

actual open class EnumMap<K : Enum<K>, V> : AbstractMutableMap<K, V>, Serializable, Cloneable {
    private val backing = TreeMap<K, V>(Comparator { a, b -> a.ordinal.compareTo(b.ordinal) })

    actual constructor(keyType: Class<K>) : super()

    actual constructor(m: EnumMap<K, out V>) : super() {
        putAll(m)
    }

    actual constructor(m: Map<K, V>) : super() {
        if (m.isEmpty() && m !is EnumMap<*, *>) {
            throw IllegalArgumentException("Specified map is empty")
        }
        putAll(m)
    }

    actual override val size: Int get() = backing.size
    actual override fun isEmpty(): Boolean = backing.isEmpty()
    actual override fun containsKey(key: K): Boolean = backing.containsKey(key)
    actual override fun containsValue(value: V): Boolean = backing.containsValue(value)
    actual override fun get(key: K): V? = backing[key]
    actual override fun put(key: K, value: V): V? = backing.put(key, value)
    actual override fun remove(key: K): V? = backing.remove(key)
    actual override fun putAll(from: Map<out K, V>) {
        for ((k, v) in from) {
            put(k, v)
        }
    }
    actual override fun clear() = backing.clear()
    actual override val keys: MutableSet<K> get() = backing.keys
    actual override val values: MutableCollection<V> get() = backing.values
    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = backing.entries

    actual open fun clone(): EnumMap<K, V> {
        return EnumMap(this)
    }
}
