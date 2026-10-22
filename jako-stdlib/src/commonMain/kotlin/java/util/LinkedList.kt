@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Cloneable

expect open class LinkedList<E> : AbstractSequentialList<E>, MutableList<E>, Deque<E>, Cloneable, Serializable {
    constructor()
    constructor(c: Collection<E>)

    override val size: Int
    override fun getFirst(): E
    override fun getLast(): E
    override fun removeFirst(): E
    override fun removeLast(): E
    override fun addFirst(e: E)
    override fun addLast(e: E)
    override fun contains(element: E): Boolean
    override fun add(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun addAll(elements: Collection<E>): Boolean
    override fun addAll(index: Int, elements: Collection<E>): Boolean
    override fun clear()
    override fun get(index: Int): E
    override fun set(index: Int, element: E): E
    override fun add(index: Int, element: E)
    override fun removeAt(index: Int): E
    override fun indexOf(element: E): Int
    override fun lastIndexOf(element: E): Int
    override fun peek(): E?
    override fun poll(): E?
    override fun offer(e: E): Boolean
    override fun offerFirst(e: E): Boolean
    override fun offerLast(e: E): Boolean
    override fun peekFirst(): E?
    override fun peekLast(): E?
    override fun pollFirst(): E?
    override fun pollLast(): E?
    override fun push(e: E)
    override fun pop(): E
    open fun removeFirstOccurrence(o: Any?): Boolean
    open fun removeLastOccurrence(o: Any?): Boolean
    override fun listIterator(index: Int): MutableListIterator<E>
    override fun descendingIterator(): MutableIterator<E>
    open fun clone(): Any
}
