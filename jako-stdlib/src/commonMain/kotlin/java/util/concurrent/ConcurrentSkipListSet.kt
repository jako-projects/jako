@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable
import java.lang.Cloneable
import java.util.NavigableSet

/**
 * [Nullability Decision] ConcurrentSkipListSet does not permit null elements.
 */
expect open class ConcurrentSkipListSet<E> : AbstractMutableSet<E>, NavigableSet<E>, Cloneable, Serializable {
    constructor()
    constructor(comparator: Comparator<in E>?)
    constructor(c: Collection<E>)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: E): Boolean
    override fun add(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun clear()
    override fun iterator(): MutableIterator<E>

    override fun descendingIterator(): MutableIterator<E>
    override fun comparator(): Comparator<in E>?
    override fun first(): E
    override fun last(): E
    open fun lower(e: E): E?
    open fun floor(e: E): E?
    open fun ceiling(e: E): E?
    open fun higher(e: E): E?
    open fun pollFirst(): E?
    open fun pollLast(): E?
}
