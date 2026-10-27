package java.util.concurrent

import java.io.Serializable
import java.lang.InterruptedException
import java.lang.Runnable
import java.lang.System
import java.lang.Thread
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal val poolThreadOwnerLock = ReentrantLock()

internal val threadToPool = HashMap<Thread, ForkJoinPool>()

internal fun currentThreadForkJoinPool(): ForkJoinPool? =
    poolThreadOwnerLock.withLock { threadToPool[Thread.currentThread()] }

actual abstract class ForkJoinTask<V> actual constructor() : Future<V>, Serializable {
    internal val taskLock = ReentrantLock()
    internal val doneCond = taskLock.newCondition()

    // Status states: 0 = NEW, 1 = RUNNING, 2 = NORMAL, 3 = CANCELLED, 4 = EXCEPTIONAL
    internal var status: Int = 0
    internal var exception: Throwable? = null
    internal var tag: Short = 0
    internal var submittedPool: ForkJoinPool? = null

    actual fun fork(): ForkJoinTask<V> {
        val pool = currentThreadForkJoinPool() ?: ForkJoinPool.commonPool()
        submittedPool = pool
        pool.enqueueTask(this)
        return this
    }

    internal fun doExec(): Boolean {
        val claim = taskLock.withLock {
            if (status != 0) return status >= 2
            status = 1
            true
        }
        if (!claim) return isDone()
        submittedPool?.removeQueuedTask(this)
        var completed = false
        try {
            completed = exec()
        } catch (ex: Throwable) {
            recordExceptionalCompletion(ex)
            return true
        }
        if (completed) {
            setNormalCompletion()
        } else {
            // For CountedCompleter, exec() returns false if still pending; only reset to 0 if not already completed
            taskLock.withLock {
                if (status == 1) {
                    status = 0
                }
            }
        }
        return isDone()
    }

    internal fun setNormalCompletion() {
        taskLock.withLock {
            if (status < 2) {
                status = 2
                doneCond.signalAll()
            }
        }
    }

    internal open fun recordExceptionalCompletion(ex: Throwable) {
        taskLock.withLock {
            if (status < 2) {
                exception = ex
                status = 4
                doneCond.signalAll()
            }
        }
    }

    private fun awaitDone(interruptible: Boolean, timed: Boolean, nanosTimeout: Long): Int {
        val canRunInline = taskLock.withLock { status == 0 }
        if (canRunInline) {
            doExec()
        }
        var remaining = nanosTimeout
        val pool = currentThreadForkJoinPool() ?: submittedPool
        while (true) {
            val s = taskLock.withLock { status }
            if (s >= 2) return s
            if (interruptible && Thread.interrupted()) throw InterruptedException()

            // Try helping execute another task from pool if we are waiting
            if (pool != null && pool.tryHelpOneTask()) {
                continue
            }

            taskLock.withLock {
                if (status >= 2) return status
                if (timed) {
                    if (remaining <= 0L) return status
                    val waitSlice = minOf(remaining, 5_000_000L) // 5ms slice to allow helping
                    val before = System.nanoTime()
                    if (interruptible) {
                        doneCond.awaitNanos(waitSlice)
                    } else {
                        try {
                            doneCond.awaitNanos(waitSlice)
                        } catch (_: InterruptedException) {
                            Thread.currentThread().interrupt()
                        }
                    }
                    remaining -= (System.nanoTime() - before)
                } else {
                    if (interruptible) {
                        doneCond.awaitNanos(5_000_000L)
                    } else {
                        try {
                            doneCond.awaitNanos(5_000_000L)
                        } catch (_: InterruptedException) {
                            Thread.currentThread().interrupt()
                        }
                    }
                }
            }
        }
    }

    private fun reportException(s: Int) {
        if (s == 3) throw CancellationException()
        if (s == 4) {
            val ex = taskLock.withLock { exception }
            when (ex) {
                is Error -> throw ex
                is RuntimeException -> throw ex
                null -> throw RuntimeException()
                else -> throw RuntimeException(ex)
            }
        }
    }

    actual fun join(): V {
        val s = awaitDone(interruptible = false, timed = false, nanosTimeout = 0L)
        if (s != 2) reportException(s)
        return getRawResult()
    }

    actual fun invoke(): V {
        doExec()
        val s = awaitDone(interruptible = false, timed = false, nanosTimeout = 0L)
        if (s != 2) reportException(s)
        return getRawResult()
    }

    actual open override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        val cancelled = taskLock.withLock {
            if (status >= 2) {
                status == 3
            } else {
                status = 3
                doneCond.signalAll()
                true
            }
        }
        if (cancelled) {
            submittedPool?.removeQueuedTask(this)
        }
        return cancelled
    }

    actual final override fun isDone(): Boolean = taskLock.withLock { status >= 2 }

    actual final override fun isCancelled(): Boolean = taskLock.withLock { status == 3 }

    actual override val isDone: Boolean
        get() = isDone()

    actual override val isCancelled: Boolean
        get() = isCancelled()

    actual fun isCompletedAbnormally(): Boolean = taskLock.withLock { status == 3 || status == 4 }

    actual fun isCompletedNormally(): Boolean = taskLock.withLock { status == 2 }

    /**
     * [Nullability Decision] getException returns Throwable? (null if not completed or completed normally).
     */
    actual fun getException(): Throwable? = taskLock.withLock {
        when (status) {
            3 -> CancellationException()
            4 -> exception
            else -> null
        }
    }

    actual open fun completeExceptionally(ex: Throwable) {
        val wrapped = if (ex is RuntimeException || ex is Error) ex else RuntimeException(ex)
        recordExceptionalCompletion(ex)
    }

    actual open fun complete(value: V) {
        try {
            setRawResult(value)
        } catch (ex: Throwable) {
            recordExceptionalCompletion(ex)
            return
        }
        setNormalCompletion()
    }

    actual fun quietlyComplete() {
        setNormalCompletion()
    }

    actual final override fun get(): V {
        if (Thread.interrupted()) throw InterruptedException()
        val s = awaitDone(interruptible = true, timed = false, nanosTimeout = 0L)
        if (s == 3) throw CancellationException()
        if (s == 4) throw ExecutionException(taskLock.withLock { exception })
        return getRawResult()
    }

    actual final override fun get(timeout: Long, unit: TimeUnit): V {
        if (Thread.interrupted()) throw InterruptedException()
        val nanos = unit.toNanos(timeout)
        val s = awaitDone(interruptible = true, timed = true, nanosTimeout = nanos)
        if (s < 2) throw TimeoutException()
        if (s == 3) throw CancellationException()
        if (s == 4) throw ExecutionException(taskLock.withLock { exception })
        return getRawResult()
    }

    actual fun quietlyJoin() {
        awaitDone(interruptible = false, timed = false, nanosTimeout = 0L)
    }

    actual fun quietlyInvoke() {
        doExec()
        awaitDone(interruptible = false, timed = false, nanosTimeout = 0L)
    }

    actual open fun reinitialize() {
        taskLock.withLock {
            exception = null
            status = 0
        }
    }

    actual open fun tryUnfork(): Boolean {
        val pool = submittedPool ?: currentThreadForkJoinPool() ?: return false
        return pool.removeQueuedTask(this)
    }

    actual abstract fun getRawResult(): V
    protected actual abstract fun setRawResult(value: V)
    protected actual abstract fun exec(): Boolean

    actual fun getForkJoinTaskTag(): Short = taskLock.withLock { tag }

    actual fun setForkJoinTaskTag(newValue: Short): Short = taskLock.withLock {
        val old = tag
        tag = newValue
        old
    }

    actual fun compareAndSetForkJoinTaskTag(expect: Short, update: Short): Boolean = taskLock.withLock {
        if (tag == expect) {
            tag = update
            true
        } else {
            false
        }
    }

    actual companion object {
        actual fun invokeAll(t1: ForkJoinTask<*>, t2: ForkJoinTask<*>) {
            t2.fork()
            t1.quietlyInvoke()
            t2.quietlyJoin()
            val ex1 = t1.getException()
            if (ex1 != null) t1.rethrow(ex1)
            val ex2 = t2.getException()
            if (ex2 != null) t2.rethrow(ex2)
        }

        actual fun invokeAll(vararg tasks: ForkJoinTask<*>) {
            if (tasks.isEmpty()) return
            val last = tasks.size - 1
            for (i in last downTo 1) {
                tasks[i].fork()
            }
            tasks[0].quietlyInvoke()
            var firstEx: Throwable? = tasks[0].getException()
            for (i in 1..last) {
                tasks[i].quietlyJoin()
                if (firstEx == null) {
                    firstEx = tasks[i].getException()
                }
            }
            if (firstEx != null) {
                tasks[0].rethrow(firstEx)
            }
        }

        actual fun <T : ForkJoinTask<*>> invokeAll(tasks: Collection<T>): Collection<T> {
            val list = tasks as? List<T> ?: tasks.toList()
            if (list.isEmpty()) return tasks
            val last = list.size - 1
            for (i in last downTo 1) {
                list[i].fork()
            }
            list[0].quietlyInvoke()
            var firstEx: Throwable? = list[0].getException()
            for (i in 1..last) {
                list[i].quietlyJoin()
                if (firstEx == null) {
                    firstEx = list[i].getException()
                }
            }
            if (firstEx != null) {
                list[0].rethrow(firstEx)
            }
            return tasks
        }

        actual fun helpQuiesce() {
            val pool = currentThreadForkJoinPool() ?: return
            while (pool.tryHelpOneTask()) {
                // Keep helping until no tasks remain
            }
        }

        actual fun getPool(): ForkJoinPool? = currentThreadForkJoinPool()

        actual fun inForkJoinPool(): Boolean = currentThreadForkJoinPool() != null

        actual fun getQueuedTaskCount(): Int = currentThreadForkJoinPool()?.getQueuedTaskCount()?.toInt() ?: 0

        actual fun getSurplusQueuedTaskCount(): Int = getQueuedTaskCount()

        actual fun adapt(runnable: Runnable): ForkJoinTask<*> =
            AdaptedRunnable<Any?>(runnable, null)

        actual fun <T> adapt(runnable: Runnable, result: T): ForkJoinTask<T> =
            AdaptedRunnable(runnable, result)

        actual fun <T> adapt(callable: Callable<out T>): ForkJoinTask<T> =
            AdaptedCallable(callable)
    }

    private fun rethrow(ex: Throwable): Nothing {
        when (ex) {
            is Error -> throw ex
            is RuntimeException -> throw ex
            else -> throw RuntimeException(ex)
        }
    }
}

internal class AdaptedRunnable<T>(
    private val runnable: Runnable,
    private var result: T
) : ForkJoinTask<T>(), RunnableFuture<T> {
    override fun getRawResult(): T = result
    override fun setRawResult(value: T) {
        result = value
    }
    override fun exec(): Boolean {
        runnable.run()
        return true
    }
    override fun run() {
        invoke()
    }
}

internal class AdaptedCallable<T>(
    private val callable: Callable<out T>
) : ForkJoinTask<T>(), RunnableFuture<T> {
    @Suppress("UNCHECKED_CAST")
    private var result: T = null as T

    override fun getRawResult(): T = result
    override fun setRawResult(value: T) {
        result = value
    }
    override fun exec(): Boolean {
        result = callable.call()
        return true
    }
    override fun run() {
        invoke()
    }
}
