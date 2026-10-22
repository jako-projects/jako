@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable
import java.lang.Cloneable

expect open class CopyOnWriteArrayList<E> : MutableList<E>, RandomAccess, Cloneable, Serializable {
    constructor()
    constructor(c: Collection<E>)
    constructor(array: Array<E>)

    open fun addIfAbsent(element: E): Boolean
    open fun addAllAbsent(c: Collection<E>): Int

    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: E): Boolean
    override fun containsAll(elements: Collection<E>): Boolean
    override fun get(index: Int): E
    override fun indexOf(element: E): Int
    override fun lastIndexOf(element: E): Int
    override fun iterator(): MutableIterator<E>
    override fun listIterator(): MutableListIterator<E>
    override fun listIterator(index: Int): MutableListIterator<E>
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E>
    override fun add(element: E): Boolean
    override fun add(index: Int, element: E)
    override fun addAll(index: Int, elements: Collection<E>): Boolean
    override fun addAll(elements: Collection<E>): Boolean
    override fun clear()
    override fun remove(element: E): Boolean
    override fun removeAll(elements: Collection<E>): Boolean
    override fun retainAll(elements: Collection<E>): Boolean
    override fun removeAt(index: Int): E
    override fun set(index: Int, element: E): E
}
