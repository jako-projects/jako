@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

import java.lang.Cloneable

/**
 * SparseIntArrays map integers to integers.
 */
expect open class SparseIntArray : Cloneable {
    constructor()
    constructor(initialCapacity: Int)

    open fun clone(): SparseIntArray
    open operator fun get(key: Int): Int
    open fun get(key: Int, valueIfKeyNotFound: Int): Int
    open fun delete(key: Int)
    open fun removeAt(index: Int)
    open fun put(key: Int, value: Int)
    open fun size(): Int
    open fun keyAt(index: Int): Int
    open fun valueAt(index: Int): Int
    open fun setValueAt(index: Int, value: Int)
    open fun indexOfKey(key: Int): Int
    open fun indexOfValue(value: Int): Int
    open fun clear()
    open fun append(key: Int, value: Int)
    open fun copyKeys(): IntArray?
    override fun toString(): String
}
