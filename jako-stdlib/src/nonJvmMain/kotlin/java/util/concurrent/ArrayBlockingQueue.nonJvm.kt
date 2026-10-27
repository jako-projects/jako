package java.util.concurrent

import java.io.Serializable
import java.util.AbstractQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual open class ArrayBlockingQueue<E> : AbstractQueue<E>, BlockingQueue<E>, Serializable {
    private val capacity: Int
    private val lock = ReentrantLock()
    private val notEmpty = lock.newCondition()
    private val notFull = lock.newCondition()
    private val items = ArrayList<E>()

    actual constructor(capacity: Int) : this(capacity, false)

    actual constructor(capacity: Int, fair: Boolean) : super() {
        if (capacity <= 0) throw IllegalArgumentException()
        this.capacity = capacity
    }

    actual constructor(capacity: Int, fair: Boolean, c: Collection<E>) : this(capacity, fair) {
        if (capacity < c.size) throw IllegalArgumentException()
        for (e in c) {
            if (e == null) throw NullPointerException()
            items.add(e)
        }
    }

    actual override val size: Int
        get() = lock.withLock { items.size }

    actual override fun add(element: E): Boolean = super.add(element)

    actual override fun offer(e: E): Boolean {
        if (e == null) throw NullPointerException()
        return lock.withLock {
            if (items.size == capacity) {
                false
            } else {
                items.add(e)
                notEmpty.signal()
                true
            }
        }
    }

    actual override fun put(e: E) {
        if (e == null) throw NullPointerException()
        lock.withLock {
            while (items.size == capacity) {
                notFull.await()
            }
            items.add(e)
            notEmpty.signal()
        }
    }

    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean {
        if (e == null) throw NullPointerException()
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            while (items.size == capacity) {
                if (nanos <= 0L) return@withLock false
                nanos = notFull.awaitNanos(nanos)
            }
            items.add(e)
            notEmpty.signal()
            true
        }
    }

    actual override fun poll(): E? = lock.withLock {
        if (items.isEmpty()) {
            null
        } else {
            val x = items.removeAt(0)
            notFull.signal()
            x
        }
    }

    actual override fun take(): E = lock.withLock {
        while (items.isEmpty()) {
            notEmpty.await()
        }
        val x = items.removeAt(0)
        notFull.signal()
        x
    }

    actual override fun poll(timeout: Long, unit: TimeUnit): E? {
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            while (items.isEmpty()) {
                if (nanos <= 0L) return@withLock null
                nanos = notEmpty.awaitNanos(nanos)
            }
            val x = items.removeAt(0)
            notFull.signal()
            x
        }
    }

    actual override fun peek(): E? = lock.withLock {
        items.firstOrNull()
    }

    actual override fun remainingCapacity(): Int = lock.withLock {
        capacity - items.size
    }

    actual override fun remove(element: E): Boolean = lock.withLock {
        if (element == null) return@withLock false
        val removed = items.remove(element)
        if (removed) notFull.signal()
        removed
    }

    actual override fun contains(element: E): Boolean = lock.withLock {
        if (element == null) false else items.contains(element)
    }

    actual override fun clear() = lock.withLock {
        val hadElements = items.isNotEmpty()
        items.clear()
        if (hadElements) notFull.signalAll()
    }

    actual override fun drainTo(c: MutableCollection<in E>): Int = drainTo(c, Int.MAX_VALUE)

    actual override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int {
        if (c === this) throw IllegalArgumentException()
        if (maxElements <= 0) return 0
        return lock.withLock {
            val n = minOf(maxElements, items.size)
            for (i in 0 until n) {
                c.add(items.removeAt(0))
            }
            if (n > 0) notFull.signalAll()
            n
        }
    }

    actual override fun iterator(): MutableIterator<E> {
        val snapshot = lock.withLock { ArrayList(items) }
        return object : MutableIterator<E> {
            private var cursor = 0
            private var lastRet = -1
            override fun hasNext(): Boolean = cursor < snapshot.size
            override fun next(): E {
                if (cursor >= snapshot.size) throw NoSuchElementException()
                lastRet = cursor
                return snapshot[cursor++]
            }
            override fun remove() {
                if (lastRet < 0) throw IllegalStateException()
                this@ArrayBlockingQueue.remove(snapshot[lastRet])
                lastRet = -1
            }
        }
    }
}
