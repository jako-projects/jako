package java.util.concurrent

import java.io.Serializable
import java.lang.InterruptedException
import java.lang.Thread
import java.util.AbstractQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class TransferSlot<E>(var item: E? = null, var fulfilled: Boolean = false)

actual open class LinkedTransferQueue<E> : AbstractQueue<E>, TransferQueue<E>, Serializable {
    private val lock = ReentrantLock()
    private val notEmpty = lock.newCondition()
    private val transferCondition = lock.newCondition()
    private val items = ArrayList<E>()
    private val waitingConsumers = ArrayList<TransferSlot<E>>()

    actual constructor() : super()

    actual constructor(c: Collection<E>) : super() {
        for (e in c) {
            if (e == null) throw NullPointerException()
            items.add(e)
        }
    }

    actual override val size: Int
        get() = lock.withLock { items.size }

    actual override fun isEmpty(): Boolean = lock.withLock { items.isEmpty() }

    actual override fun add(element: E): Boolean = offer(element)

    actual override fun offer(e: E): Boolean {
        if (e == null) throw NullPointerException()
        lock.withLock {
            if (waitingConsumers.isNotEmpty()) {
                val slot = waitingConsumers.removeAt(0)
                slot.item = e
                slot.fulfilled = true
                notEmpty.signalAll()
                transferCondition.signalAll()
            } else {
                items.add(e)
                notEmpty.signal()
            }
        }
        return true
    }

    actual override fun put(e: E) {
        offer(e)
    }

    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean = offer(e)

    actual override fun tryTransfer(e: E): Boolean {
        if (e == null) throw NullPointerException()
        return lock.withLock {
            if (waitingConsumers.isNotEmpty()) {
                val slot = waitingConsumers.removeAt(0)
                slot.item = e
                slot.fulfilled = true
                notEmpty.signalAll()
                transferCondition.signalAll()
                true
            } else {
                false
            }
        }
    }

    actual override fun transfer(e: E) {
        if (e == null) throw NullPointerException()
        if (Thread.interrupted()) throw InterruptedException()
        lock.withLock {
            if (waitingConsumers.isNotEmpty()) {
                val slot = waitingConsumers.removeAt(0)
                slot.item = e
                slot.fulfilled = true
                notEmpty.signalAll()
                transferCondition.signalAll()
                return
            }
            items.add(e)
            notEmpty.signal()
            while (items.contains(e)) {
                transferCondition.await()
            }
        }
    }

    actual override fun tryTransfer(e: E, timeout: Long, unit: TimeUnit): Boolean {
        if (e == null) throw NullPointerException()
        if (Thread.interrupted()) throw InterruptedException()
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            if (waitingConsumers.isNotEmpty()) {
                val slot = waitingConsumers.removeAt(0)
                slot.item = e
                slot.fulfilled = true
                notEmpty.signalAll()
                transferCondition.signalAll()
                return@withLock true
            }
            if (nanos <= 0L) return@withLock false
            items.add(e)
            notEmpty.signal()
            while (items.contains(e)) {
                if (nanos <= 0L) {
                    val removed = items.remove(e)
                    return@withLock !removed
                }
                nanos = transferCondition.awaitNanos(nanos)
            }
            true
        }
    }

    actual override fun poll(): E? = lock.withLock {
        if (items.isEmpty()) {
            null
        } else {
            val x = items.removeAt(0)
            transferCondition.signalAll()
            x
        }
    }

    actual override fun take(): E {
        if (Thread.interrupted()) throw InterruptedException()
        return lock.withLock {
            if (items.isNotEmpty()) {
                val x = items.removeAt(0)
                transferCondition.signalAll()
                return@withLock x
            }
            val slot = TransferSlot<E>()
            waitingConsumers.add(slot)
            try {
                while (!slot.fulfilled) {
                    if (items.isNotEmpty()) {
                        waitingConsumers.remove(slot)
                        val x = items.removeAt(0)
                        transferCondition.signalAll()
                        return@withLock x
                    }
                    notEmpty.await()
                }
                slot.item!!
            } catch (ie: InterruptedException) {
                waitingConsumers.remove(slot)
                throw ie
            }
        }
    }

    actual override fun poll(timeout: Long, unit: TimeUnit): E? {
        if (Thread.interrupted()) throw InterruptedException()
        var nanos = unit.toNanos(timeout)
        return lock.withLock {
            if (items.isNotEmpty()) {
                val x = items.removeAt(0)
                transferCondition.signalAll()
                return@withLock x
            }
            if (nanos <= 0L) return@withLock null
            val slot = TransferSlot<E>()
            waitingConsumers.add(slot)
            try {
                while (!slot.fulfilled) {
                    if (items.isNotEmpty()) {
                        waitingConsumers.remove(slot)
                        val x = items.removeAt(0)
                        transferCondition.signalAll()
                        return@withLock x
                    }
                    if (nanos <= 0L) {
                        waitingConsumers.remove(slot)
                        return@withLock if (slot.fulfilled) slot.item else null
                    }
                    nanos = notEmpty.awaitNanos(nanos)
                }
                slot.item
            } catch (ie: InterruptedException) {
                waitingConsumers.remove(slot)
                throw ie
            }
        }
    }

    actual override fun peek(): E? = lock.withLock { items.firstOrNull() }

    actual override fun hasWaitingConsumer(): Boolean = lock.withLock { waitingConsumers.isNotEmpty() }

    actual override fun getWaitingConsumerCount(): Int = lock.withLock { waitingConsumers.size }

    actual override fun remainingCapacity(): Int = Int.MAX_VALUE

    actual override fun remove(element: E): Boolean = lock.withLock {
        if (element == null) return@withLock false
        val removed = items.remove(element)
        if (removed) transferCondition.signalAll()
        removed
    }

    actual override fun contains(element: E): Boolean = lock.withLock {
        if (element == null) false else items.contains(element)
    }

    actual override fun clear() = lock.withLock {
        items.clear()
        transferCondition.signalAll()
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
            if (n > 0) transferCondition.signalAll()
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
                this@LinkedTransferQueue.remove(snapshot[lastRet])
                lastRet = -1
            }
        }
    }
}
