@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable

/**
 * [Nullability Decision] PriorityQueue does not permit null elements in JDK 8.
 * peek() and poll() return E? (null when queue is empty).
 * comparator() returns Comparator<in E>? (null when natural ordering is used).
 */
expect open class PriorityQueue<E> : AbstractQueue<E>, Serializable {
    constructor()
    constructor(initialCapacity: Int)
    constructor(comparator: Comparator<in E>?)
    constructor(initialCapacity: Int, comparator: Comparator<in E>?)
    constructor(c: Collection<E>)
    constructor(c: PriorityQueue<out E>)
    constructor(c: SortedSet<out E>)

    override val size: Int
    override fun iterator(): MutableIterator<E>
    override fun add(element: E): Boolean
    override fun offer(e: E): Boolean
    override fun peek(): E?
    override fun poll(): E?
    override fun remove(element: E): Boolean
    override fun contains(element: E): Boolean
    override fun clear()
    open fun comparator(): Comparator<in E>?
}
