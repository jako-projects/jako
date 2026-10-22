package java.util.concurrent

import java.util.AbstractQueue
import java.util.PriorityQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual open class DelayQueue<E : Delayed> actual constructor() : AbstractQueue<E>(), BlockingQueue<E> {
    private val lock = ReentrantLock()
    private val available = lock.newCondition()
    private val pq = PriorityQueue<E>()

    actual constructor(c: Collection<E>) : this() {
        addAll(c)
    }

    actual override val size: Int
        get() = lock.withLock { pq.size }

    actual override fun add(element: E): Boolean = offer(element)

    actual override fun offer(e: E): Boolean {
        return lock.withLock {
            pq.offer(e)
            if (pq.peek() === e) {
                available.signal()
            }
            true
        }
    }

    actual override fun put(e: E) {
        offer(e)
    }

    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean = offer(e)

    actual override fun poll(): E? = lock.withLock {
        val first = pq.peek()
        if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0L) {
            null
        } else {
            pq.poll()
        }
    }

    actual override fun take(): E = lock.withLock {
        while (true) {
            val first = pq.peek()
            if (first == null) {
                available.await()
            } else {
                val delay = first.getDelay(TimeUnit.NANOSECONDS)
                if (delay <= 0L) {
                    val x = pq.poll()!!
                    if (pq.isNotEmpty()) {
                        available.signal()
                    }
                    return@withLock x
                }
                available.awaitNanos(delay)
            }
        }
        @Suppress("UNREACHABLE_CODE")
        throw IllegalStateException()
    }

    actual override fun poll(timeout: Long, unit: TimeUnit): E? {
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            while (true) {
                val first = pq.peek()
                if (first == null) {
                    if (nanos <= 0L) return@withLock null
                    nanos = available.awaitNanos(nanos)
                } else {
                    val delay = first.getDelay(TimeUnit.NANOSECONDS)
                    if (delay <= 0L) {
                        val x = pq.poll()
                        if (pq.isNotEmpty()) {
                            available.signal()
                        }
                        return@withLock x
                    }
                    if (nanos <= 0L) return@withLock null
                    val waitTime = minOf(nanos, delay)
                    val left = available.awaitNanos(waitTime)
                    nanos -= (waitTime - left)
                }
            }
            @Suppress("UNREACHABLE_CODE")
            null
        }
    }

    actual override fun peek(): E? = lock.withLock { pq.peek() }

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
                val first = pq.peek()
                if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0L) break
                c.add(pq.poll()!!)
                n++
            }
            if (n > 0 && pq.isNotEmpty()) {
                available.signal()
            }
            n
        }
    }

    actual override fun iterator(): MutableIterator<E> {
        val snapshot = lock.withLock { ArrayList(pq) }
        return object : MutableIterator<E> {
            private val it = snapshot.iterator()
            private var lastRet: E? = null

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): E {
                val e = it.next()
                lastRet = e
                return e
            }
            override fun remove() {
                val e = lastRet ?: throw IllegalStateException()
                lastRet = null
                this@DelayQueue.remove(e)
            }
        }
    }
}
