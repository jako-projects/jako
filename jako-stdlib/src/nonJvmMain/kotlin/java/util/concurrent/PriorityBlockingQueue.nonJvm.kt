package java.util.concurrent

import java.io.Serializable
import java.util.AbstractQueue
import java.util.PriorityQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual open class PriorityBlockingQueue<E> : AbstractQueue<E>, BlockingQueue<E>, Serializable {
    private val lock = ReentrantLock()
    private val notEmpty = lock.newCondition()
    private val pq: PriorityQueue<E>

    actual constructor() : this(11, null)

    actual constructor(initialCapacity: Int) : this(initialCapacity, null)

    actual constructor(initialCapacity: Int, comparator: Comparator<in E>?) : super() {
        if (initialCapacity < 1) throw IllegalArgumentException()
        this.pq = PriorityQueue(initialCapacity, comparator)
    }

    actual constructor(c: Collection<E>) : super() {
        this.pq = PriorityQueue(c)
    }

    actual override val size: Int
        get() = lock.withLock { pq.size }

    actual override fun add(element: E): Boolean = offer(element)

    actual override fun offer(e: E): Boolean {
        if (e == null) throw NullPointerException()
        return lock.withLock {
            pq.offer(e)
            notEmpty.signal()
            true
        }
    }

    actual override fun put(e: E) {
        offer(e)
    }

    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean = offer(e)

    actual override fun poll(): E? = lock.withLock { pq.poll() }

    actual override fun take(): E = lock.withLock {
        while (pq.isEmpty()) {
            notEmpty.await()
        }
        pq.poll()!!
    }

    actual override fun poll(timeout: Long, unit: TimeUnit): E? {
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            while (pq.isEmpty()) {
                if (nanos <= 0L) return@withLock null
                nanos = notEmpty.awaitNanos(nanos)
            }
            pq.poll()
        }
    }

    actual override fun peek(): E? = lock.withLock { pq.peek() }

    actual open fun comparator(): Comparator<in E>? = pq.comparator()

    actual override fun remainingCapacity(): Int = Int.MAX_VALUE

    actual override fun remove(element: E): Boolean = lock.withLock { pq.remove(element) }

    actual override fun contains(element: E): Boolean = lock.withLock { pq.contains(element) }

    actual override fun clear() = lock.withLock { pq.clear() }

    actual override fun drainTo(c: MutableCollection<in E>): Int = drainTo(c, Int.MAX_VALUE)

    actual override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int {
        if (c === this) throw IllegalArgumentException()
        if (maxElements <= 0) return 0
        return lock.withLock {
            var n = 0
            while (n < maxElements) {
                val e = pq.poll() ?: break
                c.add(e)
                n++
            }
            n
        }
    }

    actual override fun iterator(): MutableIterator<E> {
        val snapshot = lock.withLock { ArrayList(pq) }
        return snapshot.iterator()
    }
}
