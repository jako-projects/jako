package java.util

import java.io.Serializable
import java.lang.Class
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

actual abstract class EnumSet<E : Enum<E>> protected constructor() : AbstractMutableSet<E>(), Cloneable, Serializable {
    actual open fun clone(): EnumSet<E> {
        return copyOf(this)
    }

    private class RegularEnumSet<E : Enum<E>> : EnumSet<E>() {
        private val backing = TreeSet<E>(Comparator { a, b -> a.ordinal.compareTo(b.ordinal) })
        override val size: Int get() = backing.size
        override fun iterator(): MutableIterator<E> = backing.iterator()
        override fun add(element: E): Boolean = backing.add(element)
        override fun remove(element: E): Boolean = backing.remove(element)
        override fun contains(element: E): Boolean = backing.contains(element)
        override fun clear() = backing.clear()
    }

    actual companion object {
        actual fun <E : Enum<E>> noneOf(elementType: Class<E>): EnumSet<E> {
            return RegularEnumSet()
        }

        actual fun <E : Enum<E>> copyOf(s: EnumSet<E>): EnumSet<E> {
            val res = RegularEnumSet<E>()
            res.addAll(s)
            return res
        }

        actual fun <E : Enum<E>> copyOf(c: Collection<E>): EnumSet<E> {
            if (c is EnumSet<E>) return copyOf(c)
            if (c.isEmpty()) throw IllegalArgumentException("Collection is empty")
            val res = RegularEnumSet<E>()
            res.addAll(c)
            return res
        }

        actual fun <E : Enum<E>> of(e: E): EnumSet<E> {
            val res = RegularEnumSet<E>()
            res.add(e)
            return res
        }

        actual fun <E : Enum<E>> of(e1: E, e2: E): EnumSet<E> {
            val res = RegularEnumSet<E>()
            res.add(e1)
            res.add(e2)
            return res
        }

        actual fun <E : Enum<E>> of(e1: E, e2: E, e3: E): EnumSet<E> {
            val res = RegularEnumSet<E>()
            res.add(e1)
            res.add(e2)
            res.add(e3)
            return res
        }

        actual fun <E : Enum<E>> of(e1: E, e2: E, e3: E, e4: E): EnumSet<E> {
            val res = RegularEnumSet<E>()
            res.add(e1)
            res.add(e2)
            res.add(e3)
            res.add(e4)
            return res
        }

        actual fun <E : Enum<E>> of(e1: E, e2: E, e3: E, e4: E, e5: E): EnumSet<E> {
            val res = RegularEnumSet<E>()
            res.add(e1)
            res.add(e2)
            res.add(e3)
            res.add(e4)
            res.add(e5)
            return res
        }

        actual fun <E : Enum<E>> of(first: E, vararg rest: E): EnumSet<E> {
            val res = RegularEnumSet<E>()
            res.add(first)
            for (e in rest) {
                res.add(e)
            }
            return res
        }
    }
}

actual interface EventListener

actual open class EventObject actual constructor(source: Any?) : Serializable {
    private val src: Any

    init {
        if (source == null) throw IllegalArgumentException("null source")
        src = source
    }

    actual open fun getSource(): Any = src
    actual open val source: Any get() = src

    actual override fun toString(): String {
        val className = this::class.qualifiedName ?: "java.util.EventObject"
        return "$className[source=$src]"
    }
}

actual fun interface Observer {
    actual fun update(o: Observable, arg: Any?)
}

actual open class Observable actual constructor() {
    private var changed = false
    private val obs = ArrayList<Observer>()

    actual open fun addObserver(o: Observer) {
        if (!obs.contains(o)) {
            obs.add(o)
        }
    }

    actual open fun deleteObserver(o: Observer?) {
        if (o != null) {
            obs.remove(o)
        }
    }

    actual open fun notifyObservers() {
        notifyObservers(null)
    }

    actual open fun notifyObservers(arg: Any?) {
        if (!changed) return
        val snapshot = ArrayList(obs)
        clearChanged()
        for (i in snapshot.size - 1 downTo 0) {
            snapshot[i].update(this, arg)
        }
    }

    actual open fun deleteObservers() {
        obs.clear()
    }

    protected actual open fun setChanged() {
        changed = true
    }

    protected actual open fun clearChanged() {
        changed = false
    }

    actual open fun hasChanged(): Boolean = changed

    actual open fun countObservers(): Int = obs.size
}

