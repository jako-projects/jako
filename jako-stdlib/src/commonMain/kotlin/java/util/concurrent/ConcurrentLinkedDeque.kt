@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable
import java.util.Deque

/**
 * [Nullability Decision] ConcurrentLinkedDeque does not permit null elements.
 * poll*, peek* return E? (null when empty).
 */
expect open class ConcurrentLinkedDeque<E> : AbstractMutableCollection<E>, Deque<E>, Serializable {
    constructor()
    constructor(c: Collection<E>)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: E): Boolean
    override fun iterator(): MutableIterator<E>
    override fun descendingIterator(): MutableIterator<E>
    override fun add(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun clear()

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
    open fun removeFirstOccurrence(o: Any?): Boolean
    open fun removeLastOccurrence(o: Any?): Boolean
    override fun offer(e: E): Boolean
    open fun remove(): E
    override fun poll(): E?
    open fun element(): E
    override fun peek(): E?
    override fun push(e: E)
    override fun pop(): E
}
