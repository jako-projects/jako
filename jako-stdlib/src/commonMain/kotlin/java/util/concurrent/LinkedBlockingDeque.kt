@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable
import java.util.AbstractQueue

expect open class LinkedBlockingDeque<E> : AbstractQueue<E>, BlockingDeque<E>, Serializable {
    constructor()
    constructor(capacity: Int)
    constructor(c: Collection<E>)

    override val size: Int
    override fun iterator(): MutableIterator<E>
    override fun offer(e: E): Boolean
    override fun put(e: E)
    override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean
    override fun remainingCapacity(): Int
    override fun drainTo(c: MutableCollection<in E>): Int
    override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int
    override fun take(): E
    override fun poll(timeout: Long, unit: TimeUnit): E?
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
