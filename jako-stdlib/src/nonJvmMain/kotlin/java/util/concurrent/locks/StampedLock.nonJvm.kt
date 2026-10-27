package java.util.concurrent.locks

import java.io.Serializable
import java.lang.InterruptedException
import java.lang.Thread
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.withLock

internal const val LG_READERS = 7

internal const val RUNIT = 1L

internal const val WBIT = 1L shl LG_READERS // 128L

internal const val RBITS = WBIT - 1L        // 127L

internal const val RFULL = RBITS - 1L       // 126L

internal const val ABITS = RBITS or WBIT    // 255L

internal const val SBITS = RBITS.inv()      // -128L

internal const val ORIGIN = WBIT shl 1      // 256L

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
        while ((state.get() and ABITS) != 0L) {
            condition.awaitUninterruptibly()
        }
        val next = state.get() + WBIT
        state.set(next)
        next
    }

    actual open fun tryWriteLock(): Long = monitor.withLock {
        val s = state.get()
        if ((s and ABITS) == 0L) {
            val next = s + WBIT
            state.set(next)
            next
        } else {
            0L
        }
    }

    actual open fun tryWriteLock(time: Long, unit: TimeUnit): Long {
        var nanos = unit.toNanos(time)
        if (Thread.interrupted()) throw InterruptedException()
        return monitor.withLock {
            while ((state.get() and ABITS) != 0L) {
                if (nanos <= 0L) return@withLock 0L
                nanos = condition.awaitNanos(nanos)
            }
            val next = state.get() + WBIT
            state.set(next)
            next
        }
    }

    actual open fun writeLockInterruptibly(): Long {
        if (Thread.interrupted()) throw InterruptedException()
        return monitor.withLock {
            while ((state.get() and ABITS) != 0L) {
                condition.await()
            }
            val next = state.get() + WBIT
            state.set(next)
            next
        }
    }

    private fun acquireReadLockInternal(): Long {
        val s = state.get()
        val m = s and ABITS
        return if (m < RFULL) {
            val next = s + RUNIT
            state.set(next)
            next
        } else {
            readerOverflow++
            s
        }
    }

    actual open fun readLock(): Long = monitor.withLock {
        while ((state.get() and WBIT) != 0L) {
            condition.awaitUninterruptibly()
        }
        acquireReadLockInternal()
    }

    actual open fun tryReadLock(): Long = monitor.withLock {
        if ((state.get() and WBIT) != 0L) {
            0L
        } else {
            acquireReadLockInternal()
        }
    }

    actual open fun tryReadLock(time: Long, unit: TimeUnit): Long {
        var nanos = unit.toNanos(time)
        if (Thread.interrupted()) throw InterruptedException()
        return monitor.withLock {
            while ((state.get() and WBIT) != 0L) {
                if (nanos <= 0L) return@withLock 0L
                nanos = condition.awaitNanos(nanos)
            }
            acquireReadLockInternal()
        }
    }

    actual open fun readLockInterruptibly(): Long {
        if (Thread.interrupted()) throw InterruptedException()
        return monitor.withLock {
            while ((state.get() and WBIT) != 0L) {
                condition.await()
            }
            acquireReadLockInternal()
        }
    }

    actual open fun tryOptimisticRead(): Long {
        val s = state.get()
        return if ((s and WBIT) == 0L) (s and SBITS) else 0L
    }

    actual open fun validate(stamp: Long): Boolean {
        return (stamp and SBITS) == (state.get() and SBITS)
    }

    actual open fun unlockWrite(stamp: Long) = monitor.withLock {
        if (state.get() != stamp || (stamp and WBIT) == 0L) {
            throw IllegalMonitorStateException()
        }
        state.set(nextWriteFinishedState(stamp))
        condition.signalAll()
    }

    actual open fun unlockRead(stamp: Long) {
        monitor.withLock {
            val s = state.get()
            val m = s and ABITS
            if ((s and SBITS) != (stamp and SBITS) || (stamp and ABITS) == 0L || m == 0L || m == WBIT) {
                throw IllegalMonitorStateException()
            }
            if (m < RFULL) {
                val next = s - RUNIT
                state.set(next)
                if ((next and ABITS) == 0L) {
                    condition.signalAll()
                }
            } else if (readerOverflow > 0) {
                readerOverflow--
            } else {
                val next = s - RUNIT
                state.set(next)
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
        val s = state.get()
        if ((s and SBITS) == (stamp and SBITS)) {
            val m = s and ABITS
            if (m == 0L) {
                if (a != 0L) return@withLock 0L
                val next = s + WBIT
                state.set(next)
                return@withLock next
            } else if (m == WBIT) {
                if (a != m) return@withLock 0L
                return@withLock stamp
            } else if (m == RUNIT && a != 0L) {
                val next = s - RUNIT + WBIT
                state.set(next)
                return@withLock next
            }
        }
        0L
    }

    actual open fun tryConvertToReadLock(stamp: Long): Long = monitor.withLock {
        val a = stamp and ABITS
        val s = state.get()
        if ((s and SBITS) == (stamp and SBITS)) {
            val m = s and ABITS
            if (m == 0L) {
                if (a != 0L) return@withLock 0L
                return@withLock acquireReadLockInternal()
            } else if (m == WBIT) {
                if (a != m) return@withLock 0L
                val next = nextWriteFinishedState(s) + RUNIT
                state.set(next)
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
        val s = state.get()
        if ((s and SBITS) == (stamp and SBITS)) {
            val m = s and ABITS
            if (m == 0L) {
                return@withLock if (a != 0L) 0L else s
            } else if (m == WBIT) {
                if (a != m) return@withLock 0L
                val next = nextWriteFinishedState(s)
                state.set(next)
                condition.signalAll()
                return@withLock next
            } else if (a != 0L && a < WBIT) {
                if (m < RFULL) {
                    val next = s - RUNIT
                    state.set(next)
                    if ((next and ABITS) == 0L) {
                        condition.signalAll()
                    }
                    return@withLock next and SBITS
                } else if (readerOverflow > 0) {
                    readerOverflow--
                    return@withLock s and SBITS
                } else {
                    val next = s - RUNIT
                    state.set(next)
                    return@withLock next and SBITS
                }
            }
        }
        0L
    }

    actual open fun tryUnlockWrite(): Boolean = monitor.withLock {
        val s = state.get()
        if ((s and WBIT) != 0L) {
            state.set(nextWriteFinishedState(s))
            condition.signalAll()
            true
        } else {
            false
        }
    }

    actual open fun tryUnlockRead(): Boolean = monitor.withLock {
        val s = state.get()
        val m = s and ABITS
        if (m != 0L && m < WBIT) {
            if (m < RFULL) {
                val next = s - RUNIT
                state.set(next)
                if ((next and ABITS) == 0L) {
                    condition.signalAll()
                }
            } else if (readerOverflow > 0) {
                readerOverflow--
            } else {
                state.set(s - RUNIT)
            }
            true
        } else {
            false
        }
    }

    actual open fun isWriteLocked(): Boolean = (state.get() and WBIT) != 0L

    actual open fun isReadLocked(): Boolean = (state.get() and RBITS) != 0L

    actual open fun getReadLockCount(): Int = monitor.withLock {
        val m = (state.get() and RBITS).toInt()
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
        val s = state.get()
        val status = when {
            (s and ABITS) == 0L -> "Unlocked"
            (s and WBIT) != 0L -> "Write-locked"
            else -> "Read-locks:${getReadLockCount()}"
        }
        return "${super.toString()}[$status]"
    }
}
