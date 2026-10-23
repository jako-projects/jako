@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

import java.lang.Cloneable

/**
 * SparseBooleanArrays map integers to booleans.
 */
expect open class SparseBooleanArray : Cloneable {
    constructor()
    constructor(initialCapacity: Int)

    open fun clone(): SparseBooleanArray
    open operator fun get(key: Int): Boolean
    open fun get(key: Int, valueIfKeyNotFound: Boolean): Boolean
    open fun delete(key: Int)
    open fun removeAt(index: Int)
    open fun put(key: Int, value: Boolean)
    open fun size(): Int
    open fun keyAt(index: Int): Int
    open fun valueAt(index: Int): Boolean
    open fun setValueAt(index: Int, value: Boolean)
    open fun indexOfKey(key: Int): Int
    open fun indexOfValue(value: Boolean): Int
    open fun clear()
    open fun append(key: Int, value: Boolean)
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
}
