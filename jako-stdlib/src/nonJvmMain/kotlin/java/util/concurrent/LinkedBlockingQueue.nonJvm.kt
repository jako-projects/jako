package java.util.concurrent

import java.io.Serializable
import java.util.AbstractQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual open class LinkedBlockingQueue<E> : AbstractQueue<E>, BlockingQueue<E>, Serializable {
    private val capacity: Int
    private val lock = ReentrantLock()
    private val notEmpty = lock.newCondition()
    private val notFull = lock.newCondition()
    private val list = mutableListOf<E>()

    actual constructor() : this(Int.MAX_VALUE)

    actual constructor(capacity: Int) : super() {
        if (capacity <= 0) throw IllegalArgumentException()
        this.capacity = capacity
    }

    actual override val size: Int get() = lock.withLock { list.size }
    actual override fun iterator(): MutableIterator<E> = lock.withLock { ArrayList(list).iterator() }
    actual override fun offer(e: E): Boolean = lock.withLock {
        if (list.size >= capacity) return@withLock false
        list.add(e)
        notEmpty.signal()
        true
    }
    actual override fun put(e: E) = lock.withLock {
        while (list.size >= capacity) {
            notFull.await()
        }
        list.add(e)
        notEmpty.signal()
    }
    actual override fun take(): E = lock.withLock {
        while (list.isEmpty()) {
            notEmpty.await()
        }
        val el = list.removeAt(0)
        notFull.signal()
        el
    }
    actual override fun poll(timeout: Long, unit: TimeUnit): E? {
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            while (list.isEmpty()) {
                if (nanos <= 0L) return@withLock null
                nanos = notEmpty.awaitNanos(nanos)
            }
            val el = list.removeAt(0)
            notFull.signal()
            el
        }
    }
    actual override fun poll(): E? = lock.withLock {
        if (list.isNotEmpty()) {
            val el = list.removeAt(0)
            notFull.signal()
            el
        } else {
            null
        }
    }
    actual override fun peek(): E? = lock.withLock { list.firstOrNull() }
    override fun remove(element: E): Boolean = lock.withLock {
        val removed = list.remove(element)
        if (removed) notFull.signal()
        removed
    }
    override fun clear() = lock.withLock {
        list.clear()
        notFull.signalAll()
    }
    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean {
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            while (list.size >= capacity) {
                if (nanos <= 0L) return@withLock false
                nanos = notFull.awaitNanos(nanos)
            }
            list.add(e)
            notEmpty.signal()
            true
        }
    }
    actual override fun remainingCapacity(): Int = lock.withLock { capacity - list.size }
    actual override fun drainTo(c: MutableCollection<in E>): Int = drainTo(c, Int.MAX_VALUE)
    actual override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int {
        if (c === this) throw IllegalArgumentException()
        if (maxElements <= 0) return 0
        return lock.withLock {
            var n = 0
            while (n < maxElements && list.isNotEmpty()) {
                val el = list.removeAt(0)
                c.add(el)
                n++
            }
            if (n > 0) notFull.signalAll()
            n
        }
    }
}
