@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.util.AbstractQueue

/**
 * [Nullability Decision] DelayQueue does not permit null elements.
 * poll() returns E? (null when empty or head not yet expired); peek() returns E? (head even if not expired).
 */
expect open class DelayQueue<E : Delayed> : AbstractQueue<E>, BlockingQueue<E> {
    constructor()
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
    override fun remainingCapacity(): Int
    override fun remove(element: E): Boolean
    override fun contains(element: E): Boolean
    override fun clear()
    override fun drainTo(c: MutableCollection<in E>): Int
    override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int
}
