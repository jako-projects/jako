@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable
import java.util.AbstractQueue

/**
 * [Nullability Decision] LinkedTransferQueue does not permit null elements; poll/peek return E?.
 */
expect open class LinkedTransferQueue<E> : AbstractQueue<E>, TransferQueue<E>, Serializable {
    constructor()
    constructor(c: Collection<E>)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun iterator(): MutableIterator<E>
    override fun add(element: E): Boolean
    override fun offer(e: E): Boolean
    override fun put(e: E)
    override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean
    override fun tryTransfer(e: E): Boolean
    override fun transfer(e: E)
    override fun tryTransfer(e: E, timeout: Long, unit: TimeUnit): Boolean
    override fun take(): E
    override fun poll(timeout: Long, unit: TimeUnit): E?
    override fun poll(): E?
    override fun peek(): E?
    override fun hasWaitingConsumer(): Boolean
    override fun getWaitingConsumerCount(): Int
    override fun remainingCapacity(): Int
    override fun remove(element: E): Boolean
    override fun contains(element: E): Boolean
    override fun clear()
    override fun drainTo(c: MutableCollection<in E>): Int
    override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int
}
