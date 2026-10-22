package java.util.concurrent

import java.io.Serializable
import java.lang.Cloneable
import java.util.NavigableMap
import java.util.TreeMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual open class ConcurrentSkipListMap<K, V> : AbstractMutableMap<K, V>, NavigableMap<K, V>, Cloneable, Serializable {
    private val lock = ReentrantLock()
    private val map: TreeMap<K, V>

    actual constructor() : super() {
        map = TreeMap()
    }

    actual constructor(comparator: Comparator<in K>?) : super() {
        map = TreeMap(comparator)
    }

    actual constructor(m: Map<out K, V>) : super() {
        map = TreeMap()
        putAll(m)
    }

    private fun checkNotNullKey(key: Any?) {
        if (key == null) throw NullPointerException("key is null")
    }

    private fun checkNotNullValue(value: Any?) {
        if (value == null) throw NullPointerException("value is null")
    }

    @Suppress("UNCHECKED_CAST")
    private fun compareKeys(k1: K, k2: K): Int {
        val cmp = map.comparator()
        return if (cmp != null) cmp.compare(k1, k2) else (k1 as Comparable<K>).compareTo(k2)
    }

    actual override val size: Int
        get() = lock.withLock { map.size }

    actual override fun isEmpty(): Boolean = lock.withLock { map.isEmpty() }

    actual override fun containsKey(key: K): Boolean {
        checkNotNullKey(key)
        return lock.withLock { map.containsKey(key) }
    }

    actual override fun containsValue(value: V): Boolean {
        checkNotNullValue(value)
        return lock.withLock { map.containsValue(value) }
    }

    actual override operator fun get(key: K): V? {
        checkNotNullKey(key)
        return lock.withLock { map[key] }
    }

    actual override fun put(key: K, value: V): V? {
        checkNotNullKey(key)
        checkNotNullValue(value)
        return lock.withLock { map.put(key, value) }
    }

    actual override fun remove(key: K): V? {
        checkNotNullKey(key)
        return lock.withLock { map.remove(key) }
    }

    actual override fun clear() = lock.withLock { map.clear() }

    actual open fun putIfAbsent(key: K, value: V): V? {
        checkNotNullKey(key)
        checkNotNullValue(value)
        return lock.withLock {
            val prev = map[key]
            if (prev == null) map.put(key, value) else prev
        }
    }

    @Suppress("UNCHECKED_CAST")
    actual open fun remove(key: Any?, value: Any?): Boolean {
        checkNotNullKey(key)
        if (value == null) return false
        return lock.withLock {
            val k = key as K
            if (map.containsKey(k) && map[k] == value) {
                map.remove(k)
                true
            } else {
                false
            }
        }
    }

    actual open fun replace(key: K, oldValue: V, newValue: V): Boolean {
        checkNotNullKey(key)
        checkNotNullValue(oldValue)
        checkNotNullValue(newValue)
        return lock.withLock {
            if (map.containsKey(key) && map[key] == oldValue) {
                map.put(key, newValue)
                true
            } else {
                false
            }
        }
    }

    actual open fun replace(key: K, value: V): V? {
        checkNotNullKey(key)
        checkNotNullValue(value)
        return lock.withLock {
            if (map.containsKey(key)) map.put(key, value) else null
        }
    }

    actual override fun comparator(): Comparator<in K>? = map.comparator()

    actual override fun firstKey(): K = lock.withLock { map.firstKey() }

    actual override fun lastKey(): K = lock.withLock { map.lastKey() }

    actual override fun lowerEntry(key: K): MutableMap.MutableEntry<K, V>? {
        checkNotNullKey(key)
        return lock.withLock { map.lowerEntry(key) }
    }

    actual override fun lowerKey(key: K): K? {
        checkNotNullKey(key)
        return lock.withLock { map.lowerKey(key) }
    }

    actual override fun floorEntry(key: K): MutableMap.MutableEntry<K, V>? {
        checkNotNullKey(key)
        return lock.withLock { map.floorEntry(key) }
    }

    actual override fun floorKey(key: K): K? {
        checkNotNullKey(key)
        return lock.withLock { map.floorKey(key) }
    }

    actual override fun ceilingEntry(key: K): MutableMap.MutableEntry<K, V>? {
        checkNotNullKey(key)
        return lock.withLock { map.ceilingEntry(key) }
    }

    actual override fun ceilingKey(key: K): K? {
        checkNotNullKey(key)
        return lock.withLock { map.ceilingKey(key) }
    }

    actual override fun higherEntry(key: K): MutableMap.MutableEntry<K, V>? {
        checkNotNullKey(key)
        return lock.withLock { map.higherEntry(key) }
    }

    actual override fun higherKey(key: K): K? {
        checkNotNullKey(key)
        return lock.withLock { map.higherKey(key) }
    }

    actual override fun firstEntry(): MutableMap.MutableEntry<K, V>? = lock.withLock {
        map.firstEntry()
    }

    actual override fun lastEntry(): MutableMap.MutableEntry<K, V>? = lock.withLock {
        map.lastEntry()
    }

    actual override fun pollFirstEntry(): MutableMap.MutableEntry<K, V>? = lock.withLock {
        map.pollFirstEntry()
    }

    actual override fun pollLastEntry(): MutableMap.MutableEntry<K, V>? = lock.withLock {
        map.pollLastEntry()
    }

    actual open fun keySet(): MutableSet<K> = keys

    internal fun snapshotKeysDescending(): MutableList<K> = lock.withLock {
        val list = ArrayList(map.keys)
        list.reverse()
        list
    }

    actual override val keys: MutableSet<K>
        get() = object : AbstractMutableSet<K>() {
            override val size: Int get() = this@ConcurrentSkipListMap.size
            override fun contains(element: K): Boolean = this@ConcurrentSkipListMap.containsKey(element)
            override fun add(element: K): Boolean = throw UnsupportedOperationException()
            override fun remove(element: K): Boolean = this@ConcurrentSkipListMap.remove(element) != null
            override fun clear() = this@ConcurrentSkipListMap.clear()
            override fun iterator(): MutableIterator<K> {
                val snapshot = lock.withLock { ArrayList(map.keys) }
                return object : MutableIterator<K> {
                    private val it = snapshot.iterator()
                    private var lastKey: K? = null
                    override fun hasNext(): Boolean = it.hasNext()
                    override fun next(): K {
                        val k = it.next()
                        lastKey = k
                        return k
                    }
                    override fun remove() {
                        val k = lastKey ?: throw IllegalStateException()
                        lastKey = null
                        this@ConcurrentSkipListMap.remove(k)
                    }
                }
            }
        }

    actual override val values: MutableCollection<V>
        get() = object : AbstractMutableCollection<V>() {
            override val size: Int get() = this@ConcurrentSkipListMap.size
            override fun contains(element: V): Boolean = this@ConcurrentSkipListMap.containsValue(element)
            override fun add(element: V): Boolean = throw UnsupportedOperationException()
            override fun clear() = this@ConcurrentSkipListMap.clear()
            override fun iterator(): MutableIterator<V> {
                val snapshot = lock.withLock { ArrayList(map.entries.map { SimpleMutableEntry(it.key, it.value) }) }
                return object : MutableIterator<V> {
                    private val it = snapshot.iterator()
                    private var lastEntry: SimpleMutableEntry<K, V>? = null
                    override fun hasNext(): Boolean = it.hasNext()
                    override fun next(): V {
                        val e = it.next()
                        lastEntry = e
                        return e.value
                    }
                    override fun remove() {
                        val e = lastEntry ?: throw IllegalStateException()
                        lastEntry = null
                        this@ConcurrentSkipListMap.remove(e.key, e.value)
                    }
                }
            }
        }

    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = object : AbstractMutableSet<MutableMap.MutableEntry<K, V>>() {
            override val size: Int get() = this@ConcurrentSkipListMap.size
            override fun add(element: MutableMap.MutableEntry<K, V>): Boolean = throw UnsupportedOperationException()
            override fun clear() = this@ConcurrentSkipListMap.clear()
            override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean =
                this@ConcurrentSkipListMap[element.key] == element.value
            override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean =
                this@ConcurrentSkipListMap.remove(element.key, element.value)
            override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> {
                val snapshot = lock.withLock { ArrayList<MutableMap.MutableEntry<K, V>>(map.entries.map { SimpleMutableEntry(it.key, it.value) }) }
                return object : MutableIterator<MutableMap.MutableEntry<K, V>> {
                    private val it = snapshot.iterator()
                    private var lastEntry: MutableMap.MutableEntry<K, V>? = null
                    override fun hasNext(): Boolean = it.hasNext()
                    override fun next(): MutableMap.MutableEntry<K, V> {
                        val e = it.next()
                        lastEntry = e
                        return e
                    }
                    override fun remove() {
                        val e = lastEntry ?: throw IllegalStateException()
                        lastEntry = null
                        this@ConcurrentSkipListMap.remove(e.key)
                    }
                }
            }
        }

    private class SimpleMutableEntry<K, V>(
        override val key: K,
        override var value: V
    ) : MutableMap.MutableEntry<K, V> {
        override fun setValue(newValue: V): V {
            throw UnsupportedOperationException()
        }
        override fun equals(other: Any?): Boolean {
            if (other !is Map.Entry<*, *>) return false
            return key == other.key && value == other.value
        }
        override fun hashCode(): Int = (key?.hashCode() ?: 0) xor (value?.hashCode() ?: 0)
        override fun toString(): String = "$key=$value"
    }
}
