package java.util.concurrent

import java.io.Serializable
import java.util.AbstractQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual open class LinkedBlockingQueue<E> : AbstractQueue<E>, BlockingQueue<E>, Serializable {
    private val lock = ReentrantLock()
    private val notEmpty = lock.newCondition()
    private val list = mutableListOf<E>()

    actual constructor() : super()
    actual constructor(capacity: Int) : super()

    actual override val size: Int get() = lock.withLock { list.size }
    actual override fun iterator(): MutableIterator<E> = lock.withLock { ArrayList(list).iterator() }
    actual override fun offer(e: E): Boolean = lock.withLock {
        list.add(e)
        notEmpty.signal()
        true
    }
    actual override fun put(e: E) {
        offer(e)
    }
    actual override fun take(): E = lock.withLock {
        while (list.isEmpty()) {
            notEmpty.await()
        }
        list.removeAt(0)
    }
    actual override fun poll(timeout: Long, unit: TimeUnit): E? {
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            while (list.isEmpty()) {
                if (nanos <= 0L) return@withLock null
                nanos = notEmpty.awaitNanos(nanos)
            }
            list.removeAt(0)
        }
    }
    actual override fun poll(): E? = lock.withLock { if (list.isNotEmpty()) list.removeAt(0) else null }
    actual override fun peek(): E? = lock.withLock { list.firstOrNull() }
    override fun remove(element: E): Boolean = lock.withLock { list.remove(element) }
    override fun clear() = lock.withLock { list.clear() }
    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean = offer(e)
    actual override fun remainingCapacity(): Int = Int.MAX_VALUE
    actual override fun drainTo(c: MutableCollection<in E>): Int = drainTo(c, Int.MAX_VALUE)
    actual override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int = lock.withLock {
        var n = 0
        while (n < maxElements && list.isNotEmpty()) {
            val el = list.removeAt(0)
            c.add(el)
            n++
        }
        n
    }
}
