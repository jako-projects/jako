@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

import java.lang.Cloneable

/**
 * SparseArray mapping longs to Objects.
 */
expect open class LongSparseArray<E> : Cloneable {
    constructor()
    constructor(initialCapacity: Int)

    open fun clone(): LongSparseArray<E>
    open operator fun get(key: Long): E?
    open fun get(key: Long, valueIfKeyNotFound: E): E
    open fun delete(key: Long)
    open fun remove(key: Long)
    open fun removeAt(index: Int)
    open fun put(key: Long, value: E)
    open fun size(): Int
    open fun keyAt(index: Int): Long
    open fun valueAt(index: Int): E
    open fun setValueAt(index: Int, value: E)
    open fun indexOfKey(key: Long): Int
    open fun indexOfValue(value: E): Int
    open fun clear()
    open fun append(key: Long, value: E)
    override fun toString(): String
}
