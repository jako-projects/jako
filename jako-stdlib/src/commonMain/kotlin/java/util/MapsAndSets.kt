@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Class
import java.lang.Cloneable

/**
 * [Nullability Decision] IdentityHashMap permits null keys and values when K/V are nullable.
 * get() and remove() return V? (null if key is not mapped or mapped to null).
 */
expect open class IdentityHashMap<K, V> : AbstractMutableMap<K, V>, MutableMap<K, V>, Serializable, Cloneable {
    constructor()
    constructor(expectedMaxSize: Int)
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

/**
 * [Nullability Decision] EnumSet does not permit null elements (E : Enum<E>).
 */
expect abstract class EnumSet<E : Enum<E>> : AbstractMutableSet<E>, Cloneable, Serializable {
    open fun clone(): EnumSet<E>

    companion object {
        fun <E : Enum<E>> noneOf(elementType: Class<E>): EnumSet<E>
        fun <E : Enum<E>> copyOf(s: EnumSet<E>): EnumSet<E>
        fun <E : Enum<E>> copyOf(c: Collection<E>): EnumSet<E>
        fun <E : Enum<E>> of(e: E): EnumSet<E>
        fun <E : Enum<E>> of(e1: E, e2: E): EnumSet<E>
        fun <E : Enum<E>> of(e1: E, e2: E, e3: E): EnumSet<E>
        fun <E : Enum<E>> of(e1: E, e2: E, e3: E, e4: E): EnumSet<E>
        fun <E : Enum<E>> of(e1: E, e2: E, e3: E, e4: E, e5: E): EnumSet<E>
        fun <E : Enum<E>> of(first: E, vararg rest: E): EnumSet<E>
    }
}

expect interface EventListener

/**
 * [Nullability Decision] EventObject constructor throws IllegalArgumentException if source is null; getSource() returns non-null Any.
 */
expect open class EventObject(source: Any?) : Serializable {
    open fun getSource(): Any
    open val source: Any
    override fun toString(): String
}

expect fun interface Observer {
    fun update(o: Observable, arg: Any?)
}

expect open class Observable() {
    open fun addObserver(o: Observer)
    open fun deleteObserver(o: Observer?)
    open fun notifyObservers()
    open fun notifyObservers(arg: Any?)
    open fun deleteObservers()
    protected open fun setChanged()
    protected open fun clearChanged()
    open fun hasChanged(): Boolean
    open fun countObservers(): Int
}

