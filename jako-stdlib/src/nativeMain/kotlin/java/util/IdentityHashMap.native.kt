package java.util

import java.io.Serializable
import java.lang.Cloneable

actual open class IdentityHashMap<K, V> : AbstractMutableMap<K, V>, MutableMap<K, V>, Serializable, Cloneable {
    private class Entry<K, V>(
        override val key: K,
        override var value: V
    ) : MutableMap.MutableEntry<K, V> {
        override fun setValue(newValue: V): V {
            val old = value
            value = newValue
            return old
        }

        override fun equals(other: Any?): Boolean {
            if (other !is Map.Entry<*, *>) return false
            return other.key === key && other.value === value
        }

        override fun hashCode(): Int {
            val kh = key?.hashCode() ?: 0
            val vh = value?.hashCode() ?: 0
            return kh xor vh
        }

        override fun toString(): String = "$key=$value"
    }

    private val entryList = ArrayList<Entry<K, V>>()

    actual constructor() : super()

    actual constructor(expectedMaxSize: Int) : super() {
        if (expectedMaxSize < 0) {
            throw IllegalArgumentException("expectedMaxSize is negative: $expectedMaxSize")
        }
    }

    actual constructor(m: Map<out K, V>) : super() {
        putAll(m)
    }

    private fun indexOfKey(key: Any?): Int {
        for (i in entryList.indices) {
            if (entryList[i].key === key) return i
        }
        return -1
    }

    actual override val size: Int get() = entryList.size

    actual override fun isEmpty(): Boolean = entryList.isEmpty()

    actual override fun get(key: K): V? {
        val idx = indexOfKey(key)
        return if (idx >= 0) entryList[idx].value else null
    }

    actual override fun containsKey(key: K): Boolean = indexOfKey(key) >= 0

    actual override fun containsValue(value: V): Boolean {
        for (i in entryList.indices) {
            if (entryList[i].value === value) return true
        }
        return false
    }

    actual override fun put(key: K, value: V): V? {
        val idx = indexOfKey(key)
        if (idx >= 0) {
            return entryList[idx].setValue(value)
        }
        entryList.add(Entry(key, value))
        return null
    }

    actual override fun putAll(from: Map<out K, V>) {
        for ((k, v) in from) {
            put(k, v)
        }
    }

    actual override fun remove(key: K): V? {
        val idx = indexOfKey(key)
        if (idx >= 0) {
            return entryList.removeAt(idx).value
        }
        return null
    }

    actual override fun clear() {
        entryList.clear()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is IdentityHashMap<*, *>) {
            if (other.size != this.size) return false
            for (e in entryList) {
                @Suppress("UNCHECKED_CAST")
                val otherMap = other as IdentityHashMap<Any?, Any?>
                val idx = otherMap.indexOfKey(e.key)
                if (idx < 0 || otherMap.entryList[idx].value !== e.value) return false
            }
            return true
        }
        if (other is Map<*, *>) {
            return entries == other.entries
        }
        return false
    }

    override fun hashCode(): Int {
        var h = 0
        for (e in entryList) {
            h += e.hashCode()
        }
        return h
    }

    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = object : AbstractMutableSet<MutableMap.MutableEntry<K, V>>() {
            override val size: Int get() = entryList.size
            override fun add(element: MutableMap.MutableEntry<K, V>): Boolean = throw UnsupportedOperationException()
            override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean {
                val idx = indexOfKey(element.key)
                return idx >= 0 && entryList[idx].value === element.value
            }
            override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean {
                val idx = indexOfKey(element.key)
                if (idx >= 0 && entryList[idx].value === element.value) {
                    entryList.removeAt(idx)
                    return true
                }
                return false
            }
            override fun clear() = entryList.clear()
            override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> =
                object : MutableIterator<MutableMap.MutableEntry<K, V>> {
                    private val it = entryList.iterator()
                    override fun hasNext(): Boolean = it.hasNext()
                    override fun next(): MutableMap.MutableEntry<K, V> = it.next()
                    override fun remove() = it.remove()
                }
        }

    actual override val keys: MutableSet<K>
        get() = super.keys

    actual override val values: MutableCollection<V>
        get() = super.values
}
