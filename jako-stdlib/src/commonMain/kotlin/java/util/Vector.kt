@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Cloneable

expect open class Vector<E> : AbstractMutableList<E>, MutableList<E>, RandomAccess, Cloneable, Serializable {
    constructor(initialCapacity: Int, capacityIncrement: Int)
    constructor(initialCapacity: Int)
    constructor()
    constructor(c: Collection<E>)

    open fun copyInto(anArray: Array<Any?>)
    open fun trimToSize()
    open fun ensureCapacity(minCapacity: Int)
    open fun setSize(newSize: Int)
    open fun capacity(): Int
    override val size: Int
    override fun isEmpty(): Boolean
    open fun elements(): Enumeration<E>
    override fun contains(element: E): Boolean
    override fun indexOf(element: E): Int
    open fun indexOf(o: Any?, index: Int): Int
    override fun lastIndexOf(element: E): Int
    open fun lastIndexOf(o: Any?, index: Int): Int
    open fun elementAt(index: Int): E
    open fun firstElement(): E
    open fun lastElement(): E
    open fun setElementAt(obj: E, index: Int)
    open fun removeElementAt(index: Int)
    open fun insertElementAt(obj: E, index: Int)
    open fun addElement(obj: E)
    open fun removeElement(obj: Any?): Boolean
    open fun removeAllElements()
    open fun clone(): Any
    override fun get(index: Int): E
    override fun set(index: Int, element: E): E
    override fun add(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun add(index: Int, element: E)
    override fun removeAt(index: Int): E
    override fun clear()
    override fun addAll(elements: Collection<E>): Boolean
    override fun addAll(index: Int, elements: Collection<E>): Boolean
    override fun iterator(): MutableIterator<E>
    override fun listIterator(): MutableListIterator<E>
    override fun listIterator(index: Int): MutableListIterator<E>
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E>
}
