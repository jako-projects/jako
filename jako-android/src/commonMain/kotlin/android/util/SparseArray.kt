@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

import java.lang.Cloneable

/**
 * SparseArrays map integers to Objects. Unlike a normal array of Objects,
 * there can be gaps in the indices.
 */
expect open class SparseArray<E> : Cloneable {
    constructor()
    constructor(initialCapacity: Int)

    open fun clone(): SparseArray<E>
    open operator fun get(key: Int): E?
    open fun get(key: Int, valueIfKeyNotFound: E): E
    open fun delete(key: Int)
    open fun remove(key: Int)
    open fun removeAt(index: Int)
    open fun removeAtRange(index: Int, size: Int)
    open fun put(key: Int, value: E)
    open fun size(): Int
    open fun keyAt(index: Int): Int
    open fun valueAt(index: Int): E
    open fun setValueAt(index: Int, value: E)
    open fun indexOfKey(key: Int): Int
    open fun indexOfValue(value: E): Int
    open fun clear()
    open fun append(key: Int, value: E)
    override fun toString(): String
}
