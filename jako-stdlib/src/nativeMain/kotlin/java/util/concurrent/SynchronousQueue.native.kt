package java.util.concurrent

import java.io.Serializable
import java.util.AbstractQueue
import kotlin.concurrent.withLock

actual open class SynchronousQueue<E> actual constructor() : AbstractQueue<E>(), BlockingQueue<E>, Serializable {
    actual override val size: Int get() = 0
    actual override fun iterator(): MutableIterator<E> = mutableListOf<E>().iterator()
    actual override fun offer(e: E): Boolean = true
    actual override fun put(e: E) {}
    actual override fun take(): E = throw NoSuchElementException()
    actual override fun poll(timeout: Long, unit: TimeUnit): E? = null
    actual override fun poll(): E? = null
    actual override fun peek(): E? = null
    override fun remove(element: E): Boolean = false
    override fun clear() {}
    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean = true
    actual override fun remainingCapacity(): Int = 0
    actual override fun drainTo(c: MutableCollection<in E>): Int = 0
    actual override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int = 0
}
