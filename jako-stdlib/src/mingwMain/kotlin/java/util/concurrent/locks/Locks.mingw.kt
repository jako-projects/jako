@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.util.concurrent.locks

import java.util.concurrent.TimeUnit
import kotlinx.cinterop.*
import platform.windows.*

internal actual fun createPlatformLock(fair: Boolean): Lock {
    return MingwReentrantLock(fair)
}

internal class MingwReentrantLock(
    private val fair: Boolean
) : ExtendedLock {

    private val arena = Arena()
    private val csPtr: CPointer<CRITICAL_SECTION> = arena.alloc<CRITICAL_SECTION>().ptr
    private var ownerThread: DWORD = 0u
    private var holdCount: Int = 0

    init {
        InitializeCriticalSection(csPtr)
    }

    val rawCsPtr: CPointer<CRITICAL_SECTION> get() = csPtr

    override fun lock() {
        EnterCriticalSection(csPtr)
        val current = GetCurrentThreadId()
        if (ownerThread == current) {
            holdCount++
        } else {
            ownerThread = current
            holdCount = 1
        }
    }

    override fun lockInterruptibly() {
        lock()
    }

    override fun tryLock(): Boolean {
        return if (TryEnterCriticalSection(csPtr) != 0) {
            val current = GetCurrentThreadId()
            if (ownerThread == current) {
                holdCount++
            } else {
                ownerThread = current
                holdCount = 1
            }
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
            platform.posix.usleep(1000u)
        }
    }

    override fun unlock() {
        check(isHeldByCurrentThread()) { "IllegalMonitorStateException: Current thread does not hold lock" }
        holdCount--
        if (holdCount == 0) {
            ownerThread = 0u
        }
        LeaveCriticalSection(csPtr)
    }

    override fun newCondition(): Condition = MingwCondition(this)

    internal fun dropOwnershipForWait(): Int {
        val saved = holdCount
        holdCount = 0
        ownerThread = 0u
        return saved
    }

    internal fun restoreOwnershipAfterWait(savedHold: Int) {
        ownerThread = GetCurrentThreadId()
        holdCount = savedHold
    }

    override fun getHoldCount(): Int = if (isHeldByCurrentThread()) holdCount else 0

    override fun isHeldByCurrentThread(): Boolean = ownerThread == GetCurrentThreadId()

    override fun isLocked(): Boolean = ownerThread != 0u

    private fun clockMonotonicNanos(): Long {
        return memScoped {
            val ts = alloc<platform.posix.timespec>()
            platform.posix.clock_gettime(platform.posix.CLOCK_MONOTONIC.convert(), ts.ptr)
            ts.tv_sec * 1_000_000_000L + ts.tv_nsec
        }
    }
}

internal class MingwCondition(
    private val lock: MingwReentrantLock
) : Condition {

    private val arena = Arena()
    private val cvPtr: CPointer<CONDITION_VARIABLE> = arena.alloc<CONDITION_VARIABLE>().ptr

    init {
        InitializeConditionVariable(cvPtr)
    }

    override fun await() {
        check(lock.isHeldByCurrentThread()) { "IllegalMonitorStateException: Lock not held" }
        val savedHold = lock.dropOwnershipForWait()
        try {
            SleepConditionVariableCS(cvPtr, lock.rawCsPtr, INFINITE)
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
        val ms = (nanosTimeout / 1_000_000L).coerceAtLeast(1L).toUInt()
        val savedHold = lock.dropOwnershipForWait()
        try {
            SleepConditionVariableCS(cvPtr, lock.rawCsPtr, ms)
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
        WakeConditionVariable(cvPtr)
    }

    override fun signalAll() {
        check(lock.isHeldByCurrentThread()) { "IllegalMonitorStateException: Lock not held" }
        WakeAllConditionVariable(cvPtr)
    }

    private fun clockRealtimeNanos(): Long {
        return memScoped {
            val ts = alloc<platform.posix.timespec>()
            platform.posix.clock_gettime(platform.posix.CLOCK_REALTIME.convert(), ts.ptr)
            ts.tv_sec * 1_000_000_000L + ts.tv_nsec
        }
    }
}
