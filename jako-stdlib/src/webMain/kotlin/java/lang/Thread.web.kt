package java.lang

import com.bennyhuo.jako.WebTaskScheduler
import com.bennyhuo.jako.annotations.WebSupport
import com.bennyhuo.jako.unsupportedOnTarget
import java.util.concurrent.atomic.AtomicReference

private const val NEW_STATE = 0
private const val RUNNABLE_STATE = 1
private const val WAITING_STATE = 2
private const val TIMED_WAITING_STATE = 3
private const val TERMINATED_STATE = 4

private var threadSeq = 0
private var threadIdSeq = 0L
private var defaultUncaughtHandler: Thread.UncaughtExceptionHandler? = null
private var activeThread: Thread? = null

private fun nextDefaultName(): String = "Thread-${threadSeq++}"
private fun nextThreadId(): Long = ++threadIdSeq

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
    internal var rawState: Int = NEW_STATE
    private var interruptedFlag: Boolean = false
    private var parkPermit: Boolean = false
    internal val parkBlocker = AtomicReference<Any?>(null)
    private var uncaughtHandler: UncaughtExceptionHandler? = null
    internal val threadLocalMap: MutableMap<ThreadLocal<*>, Any?> = HashMap()

    actual constructor() : this(null, nextDefaultName())

    actual constructor(target: Runnable?) : this(target, nextDefaultName())

    actual constructor(name: String) : this(null, name)

    actual constructor(target: Runnable?, name: String) {
        // [Nullability Decision] Java allows a null Runnable; run() is then a no-op unless subclassed.
        this.target = target
        this.threadName = name
        val parent = activeThread
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

    @WebSupport(
        reason = "JavaScript and WebAssembly runtimes are single-threaded and do not support preemptive shared-memory threads",
        alternative = "kotlinx.coroutines or java.util.concurrent.Executors / HandlerThread"
    )
    actual open fun start() {
        if (rawState != NEW_STATE) {
            throw IllegalThreadStateException("Thread already started")
        }
        unsupportedOnTarget(
            api = "java.lang.Thread.start()",
            target = "JS/Wasm",
            reason = "JavaScript and WebAssembly runtimes are single-threaded and do not support preemptive shared-memory threads",
            alternative = "kotlinx.coroutines or java.util.concurrent.Executors / HandlerThread"
        )
    }

    actual override fun run() {
        target?.run()
    }

    actual open fun interrupt() {
        interruptedFlag = true
        unpark()
    }

    actual open fun isInterrupted(): Boolean = interruptedFlag

    actual open fun isAlive(): Boolean {
        return rawState != NEW_STATE && rawState != TERMINATED_STATE
    }

    actual open fun getName(): String = threadName

    actual open fun setName(name: String) {
        threadName = name
    }

    actual open fun getId(): Long = tid

    actual open fun isDaemon(): Boolean = daemon

    actual open fun setDaemon(on: Boolean) {
        if (rawState != NEW_STATE) {
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

    actual open fun getState(): State = when (rawState) {
        NEW_STATE -> State.NEW
        RUNNABLE_STATE -> State.RUNNABLE
        WAITING_STATE -> State.WAITING
        TIMED_WAITING_STATE -> State.TIMED_WAITING
        TERMINATED_STATE -> State.TERMINATED
        else -> State.RUNNABLE
    }

    actual open fun getUncaughtExceptionHandler(): UncaughtExceptionHandler? =
        uncaughtHandler ?: defaultUncaughtHandler

    actual open fun setUncaughtExceptionHandler(eh: UncaughtExceptionHandler?) {
        uncaughtHandler = eh
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
        if (!isAlive()) return
        val current = currentThread()
        current.throwIfInterrupted()
        val waitNanos = if (millis > 0L || nanos > 0) millis * 1_000_000L + nanos else 5_000_000_000L
        WebTaskScheduler.sleepOrWait(waitNanos) { !isAlive() || current.isInterrupted() }
        current.throwIfInterrupted()
    }

    internal fun unpark() {
        parkPermit = true
    }

    internal fun lockSupportPark(isAbsolute: Boolean, time: Long) {
        if (parkPermit) {
            parkPermit = false
            return
        }
        if (isInterrupted()) return
        val prevState = rawState
        rawState = if (time > 0L || isAbsolute) TIMED_WAITING_STATE else WAITING_STATE
        try {
            val nanos = when {
                isAbsolute -> maxOf(0L, (time - System.currentTimeMillis()) * 1_000_000L)
                time > 0L -> time
                else -> 1_000_000_000L
            }
            if (nanos > 0L) {
                WebTaskScheduler.sleepOrWait(nanos) {
                    parkPermit || isInterrupted() || (!isAbsolute && time == 0L && !WebTaskScheduler.hasPendingWork())
                }
            } else {
                WebTaskScheduler.pumpReadyTasks()
            }
            parkPermit = false
        } finally {
            rawState = prevState
        }
    }

    internal fun markAttachedAndRunning() {
        if (rawState == NEW_STATE) {
            rawState = RUNNABLE_STATE
        }
    }

    internal fun markTerminated() {
        rawState = TERMINATED_STATE
    }

    internal fun clearInterrupted(): Boolean {
        val prev = interruptedFlag
        interruptedFlag = false
        return prev
    }

    internal fun parkNanos(nanos: Long) {
        if (nanos <= 0L) {
            throwIfInterrupted()
            WebTaskScheduler.pumpReadyTasks()
            return
        }
        val prevState = rawState
        rawState = TIMED_WAITING_STATE
        try {
            WebTaskScheduler.sleepOrWait(nanos) { isInterrupted() }
            throwIfInterrupted()
        } finally {
            rawState = prevState
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
            defaultUncaughtHandler

        actual fun setDefaultUncaughtExceptionHandler(eh: UncaughtExceptionHandler?) {
            defaultUncaughtHandler = eh
        }

        actual fun currentThread(): Thread {
            activeThread?.let { return it }
            val main = Thread("main")
            main.markAttachedAndRunning()
            activeThread = main
            return main
        }

        internal fun <T> withCurrentThread(thread: Thread, block: () -> T): T {
            val prev = activeThread
            activeThread = thread
            try {
                return block()
            } finally {
                activeThread = prev
            }
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
                WebTaskScheduler.pumpReadyTasks()
                return
            }
            current.parkNanos(millis * 1_000_000L + nanos)
        }

        actual fun yield() {
            WebTaskScheduler.pumpReadyTasks()
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

internal actual val Thread.threadLocalValues: MutableMap<ThreadLocal<*>, Any?>
    get() = this.threadLocalMap

internal actual var Thread.parkBlockerValue: Any?
    get() = this.parkBlocker.get()
    set(value) {
        this.parkBlocker.set(value)
    }

internal actual fun Thread.unparkInternal() {
    this.unpark()
}

internal actual fun Thread.lockSupportParkInternal(isAbsolute: Boolean, time: Long) {
    this.lockSupportPark(isAbsolute, time)
}

