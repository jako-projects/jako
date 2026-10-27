package java.util.concurrent.locks

import com.bennyhuo.jako.WebTaskScheduler
import java.lang.IllegalMonitorStateException
import java.lang.InterruptedException
import java.lang.Thread
import java.util.concurrent.TimeUnit

internal actual fun createPlatformLock(fair: Boolean): Lock {
    return WebReentrantLock(fair)
}

internal class WebReentrantLock(
    @Suppress("UNUSED_PARAMETER") private val fair: Boolean
) : ExtendedLock {

    private var ownerThread: Thread? = null
    private var holdCount: Int = 0

    override fun lock() {
        val current = Thread.currentThread()
        if (ownerThread === current) {
            holdCount++
            return
        }
        while (ownerThread != null && ownerThread !== current) {
            if (!WebTaskScheduler.pumpReadyTasks()) {
                break
            }
        }
        ownerThread = current
        holdCount = 1
    }

    override fun lockInterruptibly() {
        if (Thread.interrupted()) throw InterruptedException()
        lock()
    }

    override fun tryLock(): Boolean {
        val current = Thread.currentThread()
        if (ownerThread === current) {
            holdCount++
            return true
        }
        if (ownerThread == null) {
            ownerThread = current
            holdCount = 1
            return true
        }
        return false
    }

    override fun tryLock(time: Long, unit: TimeUnit): Boolean {
        if (Thread.interrupted()) throw InterruptedException()
        if (tryLock()) return true
        val nanos = unit.toNanos(time)
        if (nanos <= 0L) return false
        WebTaskScheduler.sleepOrWait(nanos) { tryLock() || Thread.currentThread().isInterrupted() }
        if (Thread.interrupted()) throw InterruptedException()
        return isHeldByCurrentThread()
    }

    override fun unlock() {
        if (!isHeldByCurrentThread()) {
            throw IllegalMonitorStateException("Current thread does not hold lock")
        }
        holdCount--
        if (holdCount == 0) {
            ownerThread = null
        }
    }

    override fun newCondition(): Condition = WebCondition(this)

    internal fun dropOwnershipForWait(): Pair<Thread?, Int> {
        val savedOwner = ownerThread
        val savedHold = holdCount
        ownerThread = null
        holdCount = 0
        return savedOwner to savedHold
    }

    internal fun restoreOwnershipAfterWait(saved: Pair<Thread?, Int>) {
        ownerThread = saved.first ?: Thread.currentThread()
        holdCount = saved.second
    }

    override fun getHoldCount(): Int = if (isHeldByCurrentThread()) holdCount else 0

    override fun isHeldByCurrentThread(): Boolean = ownerThread === Thread.currentThread()

    override fun isLocked(): Boolean = ownerThread != null
}

internal class WebCondition(
    private val lock: WebReentrantLock
) : Condition {

    private class Waiter(var signaled: Boolean = false)

    private val waiters = ArrayList<Waiter>()

    override fun await() {
        if (!lock.isHeldByCurrentThread()) {
            throw IllegalMonitorStateException("Lock not held by current thread")
        }
        if (Thread.interrupted()) throw InterruptedException()
        val waiter = Waiter()
        waiters.add(waiter)
        val saved = lock.dropOwnershipForWait()
        try {
            WebTaskScheduler.sleepOrWait(5_000_000_000L) {
                waiter.signaled || Thread.currentThread().isInterrupted() || !WebTaskScheduler.hasPendingWork()
            }
            if (Thread.interrupted()) throw InterruptedException()
        } finally {
            waiters.remove(waiter)
            lock.restoreOwnershipAfterWait(saved)
        }
    }

    override fun awaitUninterruptibly() {
        if (!lock.isHeldByCurrentThread()) {
            throw IllegalMonitorStateException("Lock not held by current thread")
        }
        val waiter = Waiter()
        waiters.add(waiter)
        val saved = lock.dropOwnershipForWait()
        var wasInterrupted = false
        try {
            while (!waiter.signaled && WebTaskScheduler.hasPendingWork()) {
                if (Thread.interrupted()) wasInterrupted = true
                WebTaskScheduler.sleepOrWait(100_000_000L) {
                    waiter.signaled || !WebTaskScheduler.hasPendingWork()
                }
            }
        } finally {
            waiters.remove(waiter)
            lock.restoreOwnershipAfterWait(saved)
            if (wasInterrupted) {
                Thread.currentThread().interrupt()
            }
        }
    }

    override fun awaitNanos(nanosTimeout: Long): Long {
        if (!lock.isHeldByCurrentThread()) {
            throw IllegalMonitorStateException("Lock not held by current thread")
        }
        if (Thread.interrupted()) throw InterruptedException()
        if (nanosTimeout <= 0L) {
            WebTaskScheduler.pumpReadyTasks()
            return 0L
        }
        val waiter = Waiter()
        waiters.add(waiter)
        val saved = lock.dropOwnershipForWait()
        try {
            val rem = WebTaskScheduler.sleepOrWait(nanosTimeout) {
                waiter.signaled || Thread.currentThread().isInterrupted()
            }
            if (Thread.interrupted()) throw InterruptedException()
            return rem
        } finally {
            waiters.remove(waiter)
            lock.restoreOwnershipAfterWait(saved)
        }
    }

    override fun await(time: Long, unit: TimeUnit): Boolean {
        val remaining = awaitNanos(unit.toNanos(time))
        return remaining > 0L
    }

    override fun signal() {
        if (!lock.isHeldByCurrentThread()) {
            throw IllegalMonitorStateException("Lock not held by current thread")
        }
        val first = waiters.firstOrNull { !it.signaled }
        if (first != null) {
            first.signaled = true
        }
    }

    override fun signalAll() {
        if (!lock.isHeldByCurrentThread()) {
            throw IllegalMonitorStateException("Lock not held by current thread")
        }
        for (w in waiters) {
            w.signaled = true
        }
    }
}
