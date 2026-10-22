@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.UnsafeNumber::class)

package java.util.concurrent.locks

import java.util.concurrent.TimeUnit
import kotlinx.cinterop.*
import platform.posix.*

internal actual fun createPlatformLock(fair: Boolean): Lock {
    return PosixReentrantLock(fair)
}

internal class PosixReentrantLock(
    private val fair: Boolean
) : ExtendedLock {

    private val arena = Arena()
    private val mutexPtr: CPointer<pthread_mutex_t> = arena.alloc<pthread_mutex_t>().ptr
    private var ownerThread: ULong = 0uL
    private var holdCount: Int = 0

    init {
        pthread_mutex_init(mutexPtr, null)
    }

    val rawMutexPtr: CPointer<pthread_mutex_t> get() = mutexPtr

    override fun lock() {
        val current = currentThreadId()
        if (ownerThread == current) {
            holdCount++
            return
        }
        if (pthread_mutex_lock(mutexPtr) == 0) {
            ownerThread = current
            holdCount = 1
        } else {
            throw IllegalStateException("Failed to acquire POSIX mutex")
        }
    }

    override fun lockInterruptibly() {
        lock()
    }

    override fun tryLock(): Boolean {
        val current = currentThreadId()
        if (ownerThread == current) {
            holdCount++
            return true
        }
        return if (pthread_mutex_trylock(mutexPtr) == 0) {
            ownerThread = current
            holdCount = 1
            true
        } else {
            false
        }
    }

    override fun tryLock(time: Long, unit: TimeUnit): Boolean {
        val nanos = unit.toNanos(time)
        val start = clockMonotonicNanos()
        while (true) {
            if (tryLock()) return true
            val elapsed = clockMonotonicNanos() - start
            if (elapsed >= nanos) return false
            usleep(1000u) // Sleep 1ms before retry
        }
    }

    override fun unlock() {
        check(isHeldByCurrentThread()) { "IllegalMonitorStateException: Current thread does not hold lock" }
        holdCount--
        if (holdCount == 0) {
            ownerThread = 0uL
            pthread_mutex_unlock(mutexPtr)
        }
    }

    override fun newCondition(): Condition = PosixCondition(this)

    internal fun dropOwnershipForWait(): Int {
        val saved = holdCount
        holdCount = 0
        ownerThread = 0uL
        return saved
    }

    internal fun restoreOwnershipAfterWait(savedHold: Int) {
        ownerThread = currentThreadId()
        holdCount = savedHold
    }

    override fun getHoldCount(): Int = if (isHeldByCurrentThread()) holdCount else 0

    override fun isHeldByCurrentThread(): Boolean = ownerThread == currentThreadId()

    override fun isLocked(): Boolean = ownerThread != 0uL

    private fun currentThreadId(): ULong {
        return pthread_self()?.toLong()?.toULong() ?: 0uL
    }

    private fun clockMonotonicNanos(): Long {
        return memScoped {
            val ts = alloc<timespec>()
            clock_gettime(CLOCK_MONOTONIC.convert(), ts.ptr)
            ts.tv_sec * 1_000_000_000L + ts.tv_nsec
        }
    }
}

internal class PosixCondition(
    private val lock: PosixReentrantLock
) : Condition {

    private val arena = Arena()
    private val condPtr: CPointer<pthread_cond_t> = arena.alloc<pthread_cond_t>().ptr

    init {
        pthread_cond_init(condPtr, null)
    }

    override fun await() {
        check(lock.isHeldByCurrentThread()) { "IllegalMonitorStateException: Lock not held" }
        val savedHold = lock.dropOwnershipForWait()
        try {
            pthread_cond_wait(condPtr, lock.rawMutexPtr)
        } finally {
            lock.restoreOwnershipAfterWait(savedHold)
        }
    }

    override fun awaitUninterruptibly() {
        await()
    }

    override fun awaitNanos(nanosTimeout: Long): Long {
        check(lock.isHeldByCurrentThread()) { "IllegalMonitorStateException: Lock not held" }
        if (nanosTimeout <= 0) return 0

        val start = clockRealtimeNanos()
        val targetNanos = start + nanosTimeout
        val sec = targetNanos / 1_000_000_000L
        val nsec = targetNanos % 1_000_000_000L

        val savedHold = lock.dropOwnershipForWait()
        try {
            memScoped {
                val ts = alloc<timespec>()
                ts.tv_sec = sec.convert()
                ts.tv_nsec = nsec.convert()
                pthread_cond_timedwait(condPtr, lock.rawMutexPtr, ts.ptr)
            }
        } finally {
            lock.restoreOwnershipAfterWait(savedHold)
        }

        val elapsed = clockRealtimeNanos() - start
        return (nanosTimeout - elapsed).coerceAtLeast(0)
    }

    override fun await(time: Long, unit: TimeUnit): Boolean {
        val remaining = awaitNanos(unit.toNanos(time))
        return remaining > 0
    }

    override fun signal() {
        check(lock.isHeldByCurrentThread()) { "IllegalMonitorStateException: Lock not held" }
        pthread_cond_signal(condPtr)
    }

    override fun signalAll() {
        check(lock.isHeldByCurrentThread()) { "IllegalMonitorStateException: Lock not held" }
        pthread_cond_broadcast(condPtr)
    }

    private fun clockRealtimeNanos(): Long {
        return memScoped {
            val ts = alloc<timespec>()
            clock_gettime(CLOCK_REALTIME.convert(), ts.ptr)
            ts.tv_sec * 1_000_000_000L + ts.tv_nsec
        }
    }
}
