package java.util.concurrent.locks

import java.io.Serializable
import java.lang.InterruptedException
import java.lang.System
import java.lang.Thread
import java.util.concurrent.TimeUnit
import kotlin.concurrent.AtomicLong
import kotlin.concurrent.withLock

actual object LockSupport {
    actual fun unpark(thread: Thread?) {
        thread?.unpark()
    }

    actual fun park(blocker: Any?) {
        val t = Thread.currentThread()
        t.parkBlocker.value = blocker
        try {
            t.lockSupportPark(false, 0L)
        } finally {
            t.parkBlocker.value = null
        }
    }

    actual fun parkNanos(blocker: Any?, nanos: Long) {
        if (nanos > 0L) {
            val t = Thread.currentThread()
            t.parkBlocker.value = blocker
            try {
                t.lockSupportPark(false, nanos)
            } finally {
                t.parkBlocker.value = null
            }
        }
    }

    actual fun parkUntil(blocker: Any?, deadline: Long) {
        val t = Thread.currentThread()
        t.parkBlocker.value = blocker
        try {
            t.lockSupportPark(true, deadline)
        } finally {
            t.parkBlocker.value = null
        }
    }

    actual fun getBlocker(t: Thread?): Any? {
        if (t == null) throw NullPointerException()
        return t.parkBlocker.value
    }

    actual fun park() {
        val t = Thread.currentThread()
        t.lockSupportPark(false, 0L)
    }

    actual fun parkNanos(nanos: Long) {
        if (nanos > 0L) {
            val t = Thread.currentThread()
            t.lockSupportPark(false, nanos)
        }
    }

    actual fun parkUntil(deadline: Long) {
        val t = Thread.currentThread()
        t.lockSupportPark(true, deadline)
    }
}

private const val LG_READERS = 7
private const val RUNIT = 1L
private const val WBIT = 1L shl LG_READERS // 128L
private const val RBITS = WBIT - 1L        // 127L
private const val RFULL = RBITS - 1L       // 126L
private const val ABITS = RBITS or WBIT    // 255L
private const val SBITS = RBITS.inv()      // -128L
private const val ORIGIN = WBIT shl 1      // 256L

actual open class StampedLock actual constructor() : Serializable {
    private val state = AtomicLong(ORIGIN)
    private var readerOverflow: Int = 0
    private val monitor = ReentrantLock()
    private val condition = monitor.newCondition()

    private fun nextWriteFinishedState(s: Long): Long {
        val next = s + WBIT
        return if (next == 0L) ORIGIN else next
    }

    actual open fun writeLock(): Long = monitor.withLock {
        while ((state.value and ABITS) != 0L) {
            condition.awaitUninterruptibly()
        }
        val next = state.value + WBIT
        state.value = next
        next
    }

    actual open fun tryWriteLock(): Long = monitor.withLock {
        val s = state.value
        if ((s and ABITS) == 0L) {
            val next = s + WBIT
            state.value = next
            next
        } else {
            0L
        }
    }

    actual open fun tryWriteLock(time: Long, unit: TimeUnit): Long {
        var nanos = unit.toNanos(time)
        if (Thread.interrupted()) throw InterruptedException()
        return monitor.withLock {
            while ((state.value and ABITS) != 0L) {
                if (nanos <= 0L) return@withLock 0L
                nanos = condition.awaitNanos(nanos)
            }
            val next = state.value + WBIT
            state.value = next
            next
        }
    }

    actual open fun writeLockInterruptibly(): Long {
        if (Thread.interrupted()) throw InterruptedException()
        return monitor.withLock {
            while ((state.value and ABITS) != 0L) {
                condition.await()
            }
            val next = state.value + WBIT
            state.value = next
            next
        }
    }

    private fun acquireReadLockInternal(): Long {
        val s = state.value
        val m = s and ABITS
        return if (m < RFULL) {
            val next = s + RUNIT
            state.value = next
            next
        } else {
            readerOverflow++
            s
        }
    }

    actual open fun readLock(): Long = monitor.withLock {
        while ((state.value and WBIT) != 0L) {
            condition.awaitUninterruptibly()
        }
        acquireReadLockInternal()
    }

    actual open fun tryReadLock(): Long = monitor.withLock {
        if ((state.value and WBIT) != 0L) {
            0L
        } else {
            acquireReadLockInternal()
        }
    }

    actual open fun tryReadLock(time: Long, unit: TimeUnit): Long {
        var nanos = unit.toNanos(time)
        if (Thread.interrupted()) throw InterruptedException()
        return monitor.withLock {
            while ((state.value and WBIT) != 0L) {
                if (nanos <= 0L) return@withLock 0L
                nanos = condition.awaitNanos(nanos)
            }
            acquireReadLockInternal()
        }
    }

    actual open fun readLockInterruptibly(): Long {
        if (Thread.interrupted()) throw InterruptedException()
        return monitor.withLock {
            while ((state.value and WBIT) != 0L) {
                condition.await()
            }
            acquireReadLockInternal()
        }
    }

    actual open fun tryOptimisticRead(): Long {
        val s = state.value
        return if ((s and WBIT) == 0L) (s and SBITS) else 0L
    }

    actual open fun validate(stamp: Long): Boolean {
        return (stamp and SBITS) == (state.value and SBITS)
    }

    actual open fun unlockWrite(stamp: Long) = monitor.withLock {
        if (state.value != stamp || (stamp and WBIT) == 0L) {
            throw IllegalMonitorStateException()
        }
        state.value = nextWriteFinishedState(stamp)
        condition.signalAll()
    }

    actual open fun unlockRead(stamp: Long) {
        monitor.withLock {
            val s = state.value
            val m = s and ABITS
            if ((s and SBITS) != (stamp and SBITS) || (stamp and ABITS) == 0L || m == 0L || m == WBIT) {
                throw IllegalMonitorStateException()
            }
            if (m < RFULL) {
                val next = s - RUNIT
                state.value = next
                if ((next and ABITS) == 0L) {
                    condition.signalAll()
                }
            } else if (readerOverflow > 0) {
                readerOverflow--
            } else {
                val next = s - RUNIT
                state.value = next
                if ((next and ABITS) == 0L) {
                    condition.signalAll()
                }
            }
        }
    }

    actual open fun unlock(stamp: Long) {
        if ((stamp and WBIT) != 0L) {
            unlockWrite(stamp)
        } else {
            unlockRead(stamp)
        }
    }

    actual open fun tryConvertToWriteLock(stamp: Long): Long = monitor.withLock {
        val a = stamp and ABITS
        val s = state.value
        if ((s and SBITS) == (stamp and SBITS)) {
            val m = s and ABITS
            if (m == 0L) {
                if (a != 0L) return@withLock 0L
                val next = s + WBIT
                state.value = next
                return@withLock next
            } else if (m == WBIT) {
                if (a != m) return@withLock 0L
                return@withLock stamp
            } else if (m == RUNIT && a != 0L) {
                val next = s - RUNIT + WBIT
                state.value = next
                return@withLock next
            }
        }
        0L
    }

    actual open fun tryConvertToReadLock(stamp: Long): Long = monitor.withLock {
        val a = stamp and ABITS
        val s = state.value
        if ((s and SBITS) == (stamp and SBITS)) {
            val m = s and ABITS
            if (m == 0L) {
                if (a != 0L) return@withLock 0L
                return@withLock acquireReadLockInternal()
            } else if (m == WBIT) {
                if (a != m) return@withLock 0L
                val next = nextWriteFinishedState(s) + RUNIT
                state.value = next
                condition.signalAll()
                return@withLock next
            } else if (a != 0L && a < WBIT) {
                return@withLock stamp
            }
        }
        0L
    }

    actual open fun tryConvertToOptimisticRead(stamp: Long): Long = monitor.withLock {
        val a = stamp and ABITS
        val s = state.value
        if ((s and SBITS) == (stamp and SBITS)) {
            val m = s and ABITS
            if (m == 0L) {
                return@withLock if (a != 0L) 0L else s
            } else if (m == WBIT) {
                if (a != m) return@withLock 0L
                val next = nextWriteFinishedState(s)
                state.value = next
                condition.signalAll()
                return@withLock next
            } else if (a != 0L && a < WBIT) {
                if (m < RFULL) {
                    val next = s - RUNIT
                    state.value = next
                    if ((next and ABITS) == 0L) {
                        condition.signalAll()
                    }
                    return@withLock next and SBITS
                } else if (readerOverflow > 0) {
                    readerOverflow--
                    return@withLock s and SBITS
                } else {
                    val next = s - RUNIT
                    state.value = next
                    return@withLock next and SBITS
                }
            }
        }
        0L
    }

    actual open fun tryUnlockWrite(): Boolean = monitor.withLock {
        val s = state.value
        if ((s and WBIT) != 0L) {
            state.value = nextWriteFinishedState(s)
            condition.signalAll()
            true
        } else {
            false
        }
    }

    actual open fun tryUnlockRead(): Boolean = monitor.withLock {
        val s = state.value
        val m = s and ABITS
        if (m != 0L && m < WBIT) {
            if (m < RFULL) {
                val next = s - RUNIT
                state.value = next
                if ((next and ABITS) == 0L) {
                    condition.signalAll()
                }
            } else if (readerOverflow > 0) {
                readerOverflow--
            } else {
                state.value = s - RUNIT
            }
            true
        } else {
            false
        }
    }

    actual open fun isWriteLocked(): Boolean = (state.value and WBIT) != 0L

    actual open fun isReadLocked(): Boolean = (state.value and RBITS) != 0L

    actual open fun getReadLockCount(): Int = monitor.withLock {
        val m = (state.value and RBITS).toInt()
        if (m >= RFULL.toInt()) RFULL.toInt() + readerOverflow else m
    }

    actual open val isWriteLocked: Boolean get() = isWriteLocked()
    actual open val isReadLocked: Boolean get() = isReadLocked()
    actual open val readLockCount: Int get() = getReadLockCount()

    actual open fun asReadLock(): Lock = object : Lock {
        override fun lock() {
            readLock()
        }
        override fun lockInterruptibly() {
            readLockInterruptibly()
        }
        override fun tryLock(): Boolean = tryReadLock() != 0L
        override fun tryLock(time: Long, unit: TimeUnit): Boolean = tryReadLock(time, unit) != 0L
        override fun unlock() {
            if (!tryUnlockRead()) throw IllegalMonitorStateException()
        }
        override fun newCondition(): Condition = throw UnsupportedOperationException()
    }

    actual open fun asWriteLock(): Lock = object : Lock {
        override fun lock() {
            writeLock()
        }
        override fun lockInterruptibly() {
            writeLockInterruptibly()
        }
        override fun tryLock(): Boolean = tryWriteLock() != 0L
        override fun tryLock(time: Long, unit: TimeUnit): Boolean = tryWriteLock(time, unit) != 0L
        override fun unlock() {
            if (!tryUnlockWrite()) throw IllegalMonitorStateException()
        }
        override fun newCondition(): Condition = throw UnsupportedOperationException()
    }

    actual open fun asReadWriteLock(): ReadWriteLock = object : ReadWriteLock {
        override fun readLock(): Lock = asReadLock()
        override fun writeLock(): Lock = asWriteLock()
    }

    actual override fun toString(): String {
        val s = state.value
        val status = when {
            (s and ABITS) == 0L -> "Unlocked"
            (s and WBIT) != 0L -> "Write-locked"
            else -> "Read-locks:${getReadLockCount()}"
        }
        return "${super.toString()}[$status]"
    }
}
