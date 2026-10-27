package java.util.concurrent

import java.lang.InterruptedException
import java.lang.Thread
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual open class Exchanger<V> actual constructor() {
    private val lock = ReentrantLock()
    private val cond = lock.newCondition()

    private class Slot<V>(val item: V) {
        var matched: Boolean = false
        var response: V? = null
    }

    private var waitingSlot: Slot<V>? = null

    @Suppress("UNCHECKED_CAST")
    actual open fun exchange(x: V): V {
        lock.withLock {
            val existing = waitingSlot
            if (existing != null) {
                waitingSlot = null
                existing.response = x
                existing.matched = true
                cond.signalAll()
                return existing.item
            }
            val mySlot = Slot(x)
            waitingSlot = mySlot
            while (!mySlot.matched) {
                if (Thread.currentThread().isInterrupted()) {
                    if (waitingSlot === mySlot) waitingSlot = null
                    throw InterruptedException()
                }
                cond.await()
            }
            return mySlot.response as V
        }
    }

    @Suppress("UNCHECKED_CAST")
    actual open fun exchange(x: V, timeout: Long, unit: TimeUnit): V {
        var nanos = unit.toNanos(timeout)
        lock.withLock {
            val existing = waitingSlot
            if (existing != null) {
                waitingSlot = null
                existing.response = x
                existing.matched = true
                cond.signalAll()
                return existing.item
            }
            val mySlot = Slot(x)
            waitingSlot = mySlot
            while (!mySlot.matched) {
                if (Thread.currentThread().isInterrupted()) {
                    if (waitingSlot === mySlot) waitingSlot = null
                    throw InterruptedException()
                }
                if (nanos <= 0L) {
                    if (waitingSlot === mySlot) waitingSlot = null
                    throw TimeoutException()
                }
                nanos = cond.awaitNanos(nanos)
            }
            return mySlot.response as V
        }
    }
}
