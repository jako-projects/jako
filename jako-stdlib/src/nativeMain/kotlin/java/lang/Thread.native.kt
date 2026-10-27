package java.lang

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.AtomicInt
import kotlin.concurrent.AtomicLong
import kotlin.concurrent.AtomicReference
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import platform.posix.sched_yield

private const val NEW_STATE = 0

private const val RUNNABLE_STATE = 1

private const val WAITING_STATE = 2

private const val TIMED_WAITING_STATE = 3

private const val TERMINATED_STATE = 4

private val threadSeq = AtomicInt(0)

private val threadIdSeq = AtomicLong(0)

private val attachedOnce = AtomicInt(0)

private val defaultUncaughtHandlerRef = AtomicReference<Thread.UncaughtExceptionHandler?>(null)

@kotlin.native.concurrent.ThreadLocal
private var boundCurrentThread: Thread? = null

private fun nextDefaultName(): String = "Thread-${threadSeq.addAndGet(1) - 1}"

private fun nextThreadId(): Long = threadIdSeq.addAndGet(1)

actual open class Thread : Runnable {
    actual enum class State {
        NEW,
        RUNNABLE,
        BLOCKED,
        WAITING,
        TIMED_WAITING,
        TERMINATED
    }

    actual fun interface UncaughtExceptionHandler {
        actual fun uncaughtException(t: Thread, e: Throwable)
    }

    private val tid: Long = nextThreadId()
    private var threadName: String
    private var target: Runnable?
    private var daemon: Boolean
    private var threadPriority: Int = NORM_PRIORITY
    private val rawState = AtomicInt(NEW_STATE)
    private val interruptedFlag = AtomicInt(0)
    private val parkPermit = AtomicInt(0)
    internal val parkBlocker = AtomicReference<Any?>(null)
    private val uncaughtHandlerRef = AtomicReference<UncaughtExceptionHandler?>(null)
    private val parkLock = ReentrantLock()
    private val parkCondition: Condition = parkLock.newCondition()
    private val completionLock = ReentrantLock()
    private val completionCondition: Condition = completionLock.newCondition()
    private val joiningOn = AtomicReference<Thread?>(null)
    internal val threadLocalMap: MutableMap<ThreadLocal<*>, Any?> = HashMap()

    actual constructor() : this(null, nextDefaultName())

    actual constructor(target: Runnable?) : this(target, nextDefaultName())

    actual constructor(name: String) : this(null, name)

    actual constructor(target: Runnable?, name: String) {
        // [Nullability Decision] Java allows a null Runnable; run() is then a no-op unless subclassed.
        this.target = target
        this.threadName = name
        val parent = boundCurrentThread
        this.daemon = parent?.daemon ?: false
        this.threadPriority = parent?.threadPriority ?: NORM_PRIORITY
        if (parent != null) {
            for ((tl, v) in parent.threadLocalMap) {
                if (tl is InheritableThreadLocal<*>) {
                    threadLocalMap[tl] = tl.copyForChild(v)
                }
            }
        }
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

    actual open var priority: Int
        get() = getPriority()
        set(value) {
            setPriority(value)
        }

    actual open val id: Long
        get() = tid

    actual open val isAlive: Boolean
        get() = isAlive()

    actual open val isInterrupted: Boolean
        get() = isInterrupted()

    actual open val state: State
        get() = getState()

    actual open var uncaughtExceptionHandler: UncaughtExceptionHandler?
        get() = getUncaughtExceptionHandler()
        set(value) {
            setUncaughtExceptionHandler(value)
        }

    actual open fun start() {
        if (!rawState.compareAndSet(NEW_STATE, RUNNABLE_STATE)) {
            throw IllegalThreadStateException("Thread already started")
        }
        val self = this
        val worker = Worker.start(errorReporting = false, name = threadName)
        worker.execute(TransferMode.SAFE, { self }) { thread ->
            boundCurrentThread = thread
            try {
                thread.run()
            } catch (t: Throwable) {
                val handler = thread.getUncaughtExceptionHandler() ?: getDefaultUncaughtExceptionHandler()
                if (handler != null) {
                    try {
                        handler.uncaughtException(thread, t)
                    } catch (_: Throwable) {
                    }
                } else {
                    t.printStackTrace()
                }
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

    actual open fun isAlive(): Boolean {
        val s = rawState.value
        return s != NEW_STATE && s != TERMINATED_STATE
    }

    actual open fun getName(): String = threadName

    actual open fun setName(name: String) {
        threadName = name
    }

    actual open fun getId(): Long = tid

    actual open fun isDaemon(): Boolean = daemon

    actual open fun setDaemon(on: Boolean) {
        if (rawState.value != NEW_STATE) {
            throw IllegalThreadStateException()
        }
        daemon = on
    }

    actual open fun getPriority(): Int = threadPriority

    actual open fun setPriority(newPriority: Int) {
        if (newPriority < MIN_PRIORITY || newPriority > MAX_PRIORITY) {
            throw IllegalArgumentException("Priority out of range: $newPriority")
        }
        threadPriority = newPriority
    }

    actual open fun getState(): State = when (rawState.value) {
        NEW_STATE -> State.NEW
        RUNNABLE_STATE -> State.RUNNABLE
        WAITING_STATE -> State.WAITING
        TIMED_WAITING_STATE -> State.TIMED_WAITING
        TERMINATED_STATE -> State.TERMINATED
        else -> State.RUNNABLE
    }

    actual open fun getUncaughtExceptionHandler(): UncaughtExceptionHandler? =
        uncaughtHandlerRef.value ?: defaultUncaughtHandlerRef.value

    actual open fun setUncaughtExceptionHandler(eh: UncaughtExceptionHandler?) {
        uncaughtHandlerRef.value = eh
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
        val prevState = current.rawState.value
        current.rawState.value = if (timed) TIMED_WAITING_STATE else WAITING_STATE
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
            current.rawState.compareAndSet(if (timed) TIMED_WAITING_STATE else WAITING_STATE, prevState)
            current.joiningOn.value = null
        }
    }

    private fun finish() {
        rawState.value = TERMINATED_STATE
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

    internal fun unpark() {
        parkLock.lock()
        try {
            parkPermit.value = 1
            parkCondition.signalAll()
        } finally {
            parkLock.unlock()
        }
    }

    internal fun lockSupportPark(isAbsolute: Boolean, time: Long) {
        if (parkPermit.compareAndSet(1, 0)) {
            return
        }
        if (isInterrupted()) {
            return
        }
        val prevState = rawState.value
        val waitState = if (time > 0L || isAbsolute) TIMED_WAITING_STATE else WAITING_STATE
        rawState.value = waitState
        parkLock.lock()
        try {
            if (parkPermit.compareAndSet(1, 0) || isInterrupted()) {
                return
            }
            if (isAbsolute) {
                val waitMillis = time - System.currentTimeMillis()
                if (waitMillis > 0L) {
                    parkCondition.awaitNanos(waitMillis * 1_000_000L)
                }
            } else if (time > 0L) {
                parkCondition.awaitNanos(time)
            } else {
                parkCondition.await()
            }
            parkPermit.value = 0
        } finally {
            parkLock.unlock()
            rawState.compareAndSet(waitState, prevState)
        }
    }

    internal fun markAttachedAndRunning() {
        rawState.compareAndSet(NEW_STATE, RUNNABLE_STATE)
    }

    internal fun clearInterrupted(): Boolean = interruptedFlag.compareAndSet(1, 0)

    internal fun parkNanos(nanos: Long) {
        if (nanos <= 0L) {
            throwIfInterrupted()
            return
        }
        val deadline = System.nanoTime() + nanos
        val prevState = rawState.value
        rawState.value = TIMED_WAITING_STATE
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
            rawState.compareAndSet(TIMED_WAITING_STATE, prevState)
        }
    }

    actual companion object {
        actual val MIN_PRIORITY: Int = 1
        actual val NORM_PRIORITY: Int = 5
        actual val MAX_PRIORITY: Int = 10

        actual var defaultUncaughtExceptionHandler: UncaughtExceptionHandler?
            get() = getDefaultUncaughtExceptionHandler()
            set(value) {
                setDefaultUncaughtExceptionHandler(value)
            }

        actual fun getDefaultUncaughtExceptionHandler(): UncaughtExceptionHandler? =
            defaultUncaughtHandlerRef.value

        actual fun setDefaultUncaughtExceptionHandler(eh: UncaughtExceptionHandler?) {
            defaultUncaughtHandlerRef.value = eh
        }

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

        actual fun holdsLock(obj: Any): Boolean = getObjectMonitor(obj).lock.isHeldByCurrentThread()
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

internal actual val Thread.threadLocalValues: MutableMap<ThreadLocal<*>, Any?>
    get() = this.threadLocalMap

internal actual var Thread.parkBlockerValue: Any?
    get() = this.parkBlocker.value
    set(value) {
        this.parkBlocker.value = value
    }

internal actual fun Thread.unparkInternal() {
    this.unpark()
}

internal actual fun Thread.lockSupportParkInternal(isAbsolute: Boolean, time: Long) {
    this.lockSupportPark(isAbsolute, time)
}

