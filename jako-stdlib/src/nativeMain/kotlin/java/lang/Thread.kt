@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlin.native.concurrent.ObsoleteWorkersApi::class
)

package java.lang

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.AtomicInt
import kotlin.concurrent.AtomicLong
import kotlin.concurrent.AtomicReference
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import platform.posix.sched_yield

private const val NEW = 0
private const val RUNNABLE = 1
private const val TERMINATED = 2

private val threadSeq = AtomicInt(0)
private val threadIdSeq = AtomicLong(0)
private val attachedOnce = AtomicInt(0)

@kotlin.native.concurrent.ThreadLocal
private var boundCurrentThread: Thread? = null

private fun nextDefaultName(): String = "Thread-${threadSeq.addAndGet(1) - 1}"

private fun nextThreadId(): Long = threadIdSeq.addAndGet(1)

actual open class Thread : Runnable {
    private val threadId: Long = nextThreadId()
    private var threadName: String
    private var target: Runnable?
    private var daemon: Boolean
    private val state = AtomicInt(NEW)
    private val interruptedFlag = AtomicInt(0)
    private val parkLock = ReentrantLock()
    private val parkCondition: Condition = parkLock.newCondition()
    private val completionLock = ReentrantLock()
    private val completionCondition: Condition = completionLock.newCondition()
    private val joiningOn = AtomicReference<Thread?>(null)
    internal val threadLocalValues: MutableMap<ThreadLocal<*>, Any?> = HashMap()

    actual constructor() : this(null, nextDefaultName())

    actual constructor(target: Runnable?) : this(target, nextDefaultName())

    actual constructor(name: String) : this(null, name)

    actual constructor(target: Runnable?, name: String) {
        // [Nullability Decision] Java allows a null Runnable; run() is then a no-op unless subclassed.
        this.target = target
        this.threadName = name
        this.daemon = boundCurrentThread?.daemon ?: false
    }

    actual open var name: String
        get() = threadName
        set(value) {
            threadName = value
        }

    actual open var isDaemon: Boolean
        get() = daemon
        set(value) {
            setDaemon(value)
        }

    actual open val id: Long
        get() = threadId

    actual open val isAlive: Boolean
        get() = isAlive()

    actual open val isInterrupted: Boolean
        get() = isInterrupted()

    actual open fun start() {
        if (!state.compareAndSet(NEW, RUNNABLE)) {
            throw IllegalThreadStateException("Thread already started")
        }
        val self = this
        val worker = Worker.start(errorReporting = false, name = threadName)
        worker.execute(TransferMode.SAFE, { self }) { thread ->
            boundCurrentThread = thread
            try {
                thread.run()
            } catch (t: Throwable) {
                t.printStackTrace()
            } finally {
                thread.finish()
                Worker.current.requestTermination()
            }
        }
    }

    actual override fun run() {
        target?.run()
    }

    actual open fun interrupt() {
        interruptedFlag.value = 1
        unpark()
        joiningOn.value?.signalCompletion()
    }

    actual open fun isInterrupted(): Boolean = interruptedFlag.value != 0

    actual open fun isAlive(): Boolean = state.value == RUNNABLE

    actual open fun getName(): String = threadName

    actual open fun setName(name: String) {
        threadName = name
    }

    actual open fun getId(): Long = threadId

    actual open fun isDaemon(): Boolean = daemon

    actual open fun setDaemon(on: Boolean) {
        if (state.value != NEW) {
            throw IllegalThreadStateException()
        }
        daemon = on
    }

    actual open fun join() {
        join(0L)
    }

    actual open fun join(millis: Long) {
        join(millis, 0)
    }

    actual open fun join(millis: Long, nanos: Int) {
        if (millis < 0L) {
            throw IllegalArgumentException("timeout value is negative")
        }
        if (nanos < 0 || nanos > 999999) {
            throw IllegalArgumentException("nanosecond timeout value out of range")
        }
        if (!isAlive()) {
            return
        }

        val current = currentThread()
        val timed = millis > 0L || nanos > 0
        val deadlineNanos = if (timed) {
            System.nanoTime() + millis * 1_000_000L + nanos
        } else {
            0L
        }

        current.joiningOn.value = this
        try {
            completionLock.lock()
            try {
                while (isAlive()) {
                    current.throwIfInterrupted()
                    if (timed) {
                        val remaining = deadlineNanos - System.nanoTime()
                        if (remaining <= 0L) {
                            return
                        }
                        completionCondition.awaitNanos(remaining)
                    } else {
                        completionCondition.await()
                    }
                }
            } finally {
                completionLock.unlock()
            }
        } finally {
            current.joiningOn.value = null
        }
    }

    private fun finish() {
        state.value = TERMINATED
        signalCompletion()
        if (boundCurrentThread === this) {
            boundCurrentThread = null
        }
    }

    private fun signalCompletion() {
        completionLock.lock()
        try {
            completionCondition.signalAll()
        } finally {
            completionLock.unlock()
        }
    }

    private fun unpark() {
        parkLock.lock()
        try {
            parkCondition.signalAll()
        } finally {
            parkLock.unlock()
        }
    }

    internal fun markAttachedAndRunning() {
        state.compareAndSet(NEW, RUNNABLE)
    }

    internal fun clearInterrupted(): Boolean = interruptedFlag.compareAndSet(1, 0)

    internal fun parkNanos(nanos: Long) {
        if (nanos <= 0L) {
            throwIfInterrupted()
            return
        }
        val deadline = System.nanoTime() + nanos
        parkLock.lock()
        try {
            while (true) {
                throwIfInterrupted()
                val remaining = deadline - System.nanoTime()
                if (remaining <= 0L) {
                    return
                }
                parkCondition.awaitNanos(remaining)
            }
        } finally {
            parkLock.unlock()
        }
    }

    actual companion object {
        actual fun currentThread(): Thread {
            boundCurrentThread?.let { return it }
            val created = attachCurrentNativeThread()
            boundCurrentThread = created
            return created
        }

        actual fun sleep(millis: Long) {
            sleep(millis, 0)
        }

        actual fun sleep(millis: Long, nanos: Int) {
            if (millis < 0L) {
                throw IllegalArgumentException("timeout value is negative")
            }
            if (nanos < 0 || nanos > 999999) {
                throw IllegalArgumentException("nanosecond timeout value out of range")
            }
            val current = currentThread()
            current.throwIfInterrupted()
            if (millis == 0L && nanos == 0) {
                return
            }
            current.parkNanos(millis * 1_000_000L + nanos)
        }

        actual fun yield() {
            sched_yield()
        }

        actual fun interrupted(): Boolean {
            return currentThread().clearInterrupted()
        }

        fun holdsLock(obj: Any): Boolean = getObjectMonitor(obj).lock.isHeldByCurrentThread()
    }
}

private fun Thread.throwIfInterrupted() {
    if (clearInterrupted()) {
        throw InterruptedException()
    }
}

private fun attachCurrentNativeThread(): Thread {
    val name = if (attachedOnce.compareAndSet(0, 1)) "main" else nextDefaultName()
    val thread = Thread(name)
    thread.markAttachedAndRunning()
    return thread
}
