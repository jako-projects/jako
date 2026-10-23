package java.util

actual open class WeakHashMap<K, V> : AbstractMutableMap<K, V>, MutableMap<K, V> {
    private val backing: LinkedHashMap<K, V>

    actual constructor(initialCapacity: Int, loadFactor: Float) : super() {
        if (initialCapacity < 0) throw IllegalArgumentException("Illegal Initial Capacity: $initialCapacity")
        if (loadFactor <= 0f || loadFactor.isNaN()) throw IllegalArgumentException("Illegal Load factor: $loadFactor")
        backing = LinkedHashMap(initialCapacity, loadFactor)
    }

    actual constructor(initialCapacity: Int) : this(initialCapacity, 0.75f)

    actual constructor() : this(16, 0.75f)

    actual constructor(m: Map<out K, V>) : this(maxOf((m.size / 0.75f).toInt() + 1, 16), 0.75f) {
        putAll(m)
    }

    actual override val size: Int get() = backing.size
    actual override fun isEmpty(): Boolean = backing.isEmpty()
    actual override fun get(key: K): V? = backing[key]
    actual override fun containsKey(key: K): Boolean = backing.containsKey(key)
    actual override fun containsValue(value: V): Boolean = backing.containsValue(value)
    actual override fun put(key: K, value: V): V? = backing.put(key, value)
    actual override fun putAll(from: Map<out K, V>) = backing.putAll(from)
    actual override fun remove(key: K): V? = backing.remove(key)
    actual override fun clear() = backing.clear()
    actual override val keys: MutableSet<K> get() = backing.keys
    actual override val values: MutableCollection<V> get() = backing.values
    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = backing.entries
}
