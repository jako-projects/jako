@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

import java.lang.Cloneable

/**
 * SparseLongArrays map integers to Long.
 */
expect open class SparseLongArray : Cloneable {
    constructor()
    constructor(initialCapacity: Int)

    open fun clone(): SparseLongArray
    open operator fun get(key: Int): Long
    open fun get(key: Int, valueIfKeyNotFound: Long): Long
    open fun delete(key: Int)
    open fun removeAt(index: Int)
    open fun put(key: Int, value: Long)
    open fun size(): Int
    open fun keyAt(index: Int): Int
    open fun valueAt(index: Int): Long
    open fun indexOfKey(key: Int): Int
    open fun indexOfValue(value: Long): Int
    open fun clear()
    open fun append(key: Int, value: Long)
    override fun toString(): String
}
