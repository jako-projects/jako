@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable
import java.util.AbstractQueue

/**
 * [Nullability Decision] PriorityBlockingQueue does not permit null elements.
 * poll() and peek() return E? (null when empty).
 */
expect open class PriorityBlockingQueue<E> : AbstractQueue<E>, BlockingQueue<E>, Serializable {
    constructor()
    constructor(initialCapacity: Int)
    constructor(initialCapacity: Int, comparator: Comparator<in E>?)
    constructor(c: Collection<E>)

    override val size: Int
    override fun iterator(): MutableIterator<E>
    override fun add(element: E): Boolean
    override fun offer(e: E): Boolean
    override fun put(e: E)
    override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean
    override fun poll(): E?
    override fun take(): E
    override fun poll(timeout: Long, unit: TimeUnit): E?
    override fun peek(): E?
    open fun comparator(): Comparator<in E>?
    override fun remainingCapacity(): Int
    override fun remove(element: E): Boolean
    override fun contains(element: E): Boolean
    override fun clear()
    override fun drainTo(c: MutableCollection<in E>): Int
    override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int
}
