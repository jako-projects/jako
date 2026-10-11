@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Cloneable

expect open class ArrayDeque<E> : AbstractQueue<E>, Deque<E>, Cloneable, Serializable {
    constructor()
    constructor(numElements: Int)
    constructor(c: Collection<E>)

    override val size: Int
    val isEmpty: Boolean
    override fun isEmpty(): Boolean
    override fun contains(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun clear()
    override fun iterator(): MutableIterator<E>

    override fun offer(e: E): Boolean
    override fun poll(): E?
    override fun peek(): E?

    override fun addFirst(e: E)
    override fun addLast(e: E)
    override fun offerFirst(e: E): Boolean
    override fun offerLast(e: E): Boolean
    override fun removeFirst(): E
    override fun removeLast(): E
    override fun pollFirst(): E?
    override fun pollLast(): E?
    override fun getFirst(): E
    override fun getLast(): E
    override fun peekFirst(): E?
    override fun peekLast(): E?
    override fun pop(): E
    override fun push(e: E)
    override fun descendingIterator(): MutableIterator<E>
}
