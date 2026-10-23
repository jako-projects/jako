@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

/**
 * ArraySet is a generic set data structure that is designed to be more memory efficient
 * than a traditional HashSet.
 */
expect final class ArraySet<E> : MutableCollection<E>, MutableSet<E> {
    constructor()
    constructor(capacity: Int)
    constructor(set: ArraySet<E>?)
    constructor(set: Collection<E>?)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: E): Boolean
    override fun iterator(): MutableIterator<E>
    override fun add(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun containsAll(elements: Collection<E>): Boolean
    override fun addAll(elements: Collection<E>): Boolean
    override fun retainAll(elements: Collection<E>): Boolean
    override fun removeAll(elements: Collection<E>): Boolean
    override fun clear()

    fun ensureCapacity(minimumCapacity: Int)
    fun valueAt(index: Int): E
    fun removeAt(index: Int): E
    fun indexOf(key: Any?): Int
    fun addAll(array: ArraySet<out E>)
    fun removeAll(array: ArraySet<out E>): Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
}
