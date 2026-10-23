@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable
import java.util.AbstractQueue

expect open class LinkedBlockingQueue<E> : AbstractQueue<E>, BlockingQueue<E>, Serializable {
    constructor()
    constructor(capacity: Int)

    override val size: Int
    override fun iterator(): MutableIterator<E>
    override fun offer(e: E): Boolean
    override fun put(e: E)
    override fun take(): E
    override fun poll(timeout: Long, unit: TimeUnit): E?
    override fun poll(): E?
    override fun peek(): E?
    override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean
    override fun remainingCapacity(): Int
    override fun drainTo(c: MutableCollection<in E>): Int
    override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int
}
