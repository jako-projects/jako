@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable
import java.util.AbstractQueue
import java.util.Queue

expect open class ConcurrentLinkedQueue<E> : AbstractQueue<E>, Queue<E>, Serializable {
    constructor()
    constructor(c: Collection<E>)

    override val size: Int
    override fun iterator(): MutableIterator<E>
    override fun offer(e: E): Boolean
    override fun poll(): E?
    override fun peek(): E?
}
