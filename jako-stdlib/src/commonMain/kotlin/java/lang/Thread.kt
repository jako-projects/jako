@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import com.bennyhuo.jako.annotations.WebSupport
import kotlin.Long
import kotlin.Int

expect open class Thread : Runnable {
    constructor()
    constructor(target: Runnable?)
    constructor(name: String)
    constructor(target: Runnable?, name: String)

    enum class State {
        NEW,
        RUNNABLE,
        BLOCKED,
        WAITING,
        TIMED_WAITING,
        TERMINATED
    }

    fun interface UncaughtExceptionHandler {
        fun uncaughtException(t: Thread, e: Throwable)
    }

    open var name: String
    open var isDaemon: Boolean
    open var priority: Int
    open val id: Long
    open val isAlive: Boolean
    open val isInterrupted: Boolean
    open val state: State
    open var uncaughtExceptionHandler: UncaughtExceptionHandler?

    @WebSupport(
        reason = "JavaScript and WebAssembly runtimes are single-threaded and do not support preemptive shared-memory threads",
        alternative = "kotlinx.coroutines or java.util.concurrent.Executors / HandlerThread"
    )
    open fun start()
    override fun run()
    open fun interrupt()
    open fun isInterrupted(): Boolean
    open fun isAlive(): Boolean
    open fun getName(): String
    open fun setName(name: String)
    open fun getId(): Long
    open fun isDaemon(): Boolean
    open fun setDaemon(on: Boolean)
    open fun getPriority(): Int
    open fun setPriority(newPriority: Int)
    open fun getState(): State
    open fun getUncaughtExceptionHandler(): UncaughtExceptionHandler?
    open fun setUncaughtExceptionHandler(eh: UncaughtExceptionHandler?)
    open fun join()
    open fun join(millis: Long)
    open fun join(millis: Long, nanos: Int)

    companion object {
        val MIN_PRIORITY: Int
        val NORM_PRIORITY: Int
        val MAX_PRIORITY: Int
        var defaultUncaughtExceptionHandler: UncaughtExceptionHandler?

        fun currentThread(): Thread
        fun sleep(millis: Long)
        fun sleep(millis: Long, nanos: Int)
        fun yield()
        fun interrupted(): Boolean
        fun holdsLock(obj: Any): Boolean
        fun getDefaultUncaughtExceptionHandler(): UncaughtExceptionHandler?
        fun setDefaultUncaughtExceptionHandler(eh: UncaughtExceptionHandler?)
    }
}
