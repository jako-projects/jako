package java.util.concurrent

import java.io.Serializable
import java.lang.InterruptedException
import java.lang.Runnable
import java.lang.System
import java.lang.Thread
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val poolThreadOwnerLock = ReentrantLock()
private val threadToPool = HashMap<Thread, ForkJoinPool>()

internal fun currentThreadForkJoinPool(): ForkJoinPool? =
    poolThreadOwnerLock.withLock { threadToPool[Thread.currentThread()] }

internal fun registerForkJoinWorker(thread: Thread, pool: ForkJoinPool) {
    poolThreadOwnerLock.withLock { threadToPool[thread] = pool }
}

internal fun unregisterForkJoinWorker(thread: Thread) {
    poolThreadOwnerLock.withLock { threadToPool.remove(thread) }
}

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

private class AdaptedRunnable<T>(
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

private class AdaptedCallable<T>(
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

actual abstract class RecursiveAction actual constructor() : ForkJoinTask<java.lang.Void?>() {
    protected actual abstract fun compute()

    actual final override fun getRawResult(): java.lang.Void? = null

    actual final override fun setRawResult(mustBeNull: java.lang.Void?) {}

    actual final override fun exec(): Boolean {
        compute()
        return true
    }
}

actual abstract class RecursiveTask<V> actual constructor() : ForkJoinTask<V>() {
    @Suppress("UNCHECKED_CAST")
    private var result: V = null as V

    protected actual abstract fun compute(): V

    actual final override fun getRawResult(): V = result

    actual final override fun setRawResult(value: V) {
        result = value
    }

    actual final override fun exec(): Boolean {
        result = compute()
        return true
    }
}

actual abstract class CountedCompleter<T> : ForkJoinTask<T> {
    private val completerRef: CountedCompleter<*>?
    private var pendingCountValue: Int = 0

    protected actual constructor(completer: CountedCompleter<*>?, initialPendingCount: Int) : super() {
        this.completerRef = completer
        this.pendingCountValue = initialPendingCount
    }

    protected actual constructor(completer: CountedCompleter<*>?) : this(completer, 0)

    protected actual constructor() : this(null, 0)

    actual abstract fun compute()

    actual open fun onCompletion(caller: CountedCompleter<*>) {}

    actual open fun onExceptionalCompletion(ex: Throwable, caller: CountedCompleter<*>): Boolean = true

    actual fun getCompleter(): CountedCompleter<*>? = completerRef

    actual fun getPendingCount(): Int = taskLock.withLock { pendingCountValue }

    actual fun setPendingCount(count: Int) {
        taskLock.withLock { pendingCountValue = count }
    }

    actual fun addToPendingCount(delta: Int) {
        taskLock.withLock { pendingCountValue += delta }
    }

    actual fun compareAndSetPendingCount(expected: Int, count: Int): Boolean = taskLock.withLock {
        if (pendingCountValue == expected) {
            pendingCountValue = count
            true
        } else {
            false
        }
    }

    actual fun decrementPendingCountUnlessZero(): Int = taskLock.withLock {
        val c = pendingCountValue
        if (c != 0) {
            pendingCountValue = c - 1
        }
        c
    }

    actual fun getRoot(): CountedCompleter<*> {
        var a: CountedCompleter<*> = this
        while (true) {
            val p = a.completerRef ?: return a
            a = p
        }
    }

    actual fun tryComplete() {
        var a: CountedCompleter<*> = this
        var s: CountedCompleter<*> = a
        while (true) {
            val wasPositive = a.taskLock.withLock {
                if (a.pendingCountValue == 0) {
                    false
                } else {
                    a.pendingCountValue -= 1
                    true
                }
            }
            if (wasPositive) return
            a.onCompletion(s)
            s = a
            val p = a.completerRef
            if (p == null) {
                a.quietlyComplete()
                return
            }
            a = p
        }
    }

    actual fun propagateCompletion() {
        var a: CountedCompleter<*> = this
        while (true) {
            val wasPositive = a.taskLock.withLock {
                if (a.pendingCountValue == 0) {
                    false
                } else {
                    a.pendingCountValue -= 1
                    true
                }
            }
            if (wasPositive) return
            val p = a.completerRef
            if (p == null) {
                a.quietlyComplete()
                return
            }
            a = p
        }
    }

    actual override fun complete(rawResult: T) {
        setRawResult(rawResult)
        onCompletion(this)
        quietlyComplete()
        completerRef?.tryComplete()
    }

    actual fun firstComplete(): CountedCompleter<*>? {
        while (true) {
            val c = decrementPendingCountUnlessZero()
            if (c == 0) return this
            return null
        }
    }

    actual fun nextComplete(): CountedCompleter<*>? {
        val p = completerRef
        if (p != null) return p.firstComplete()
        quietlyComplete()
        return null
    }

    actual fun quietlyCompleteRoot() {
        getRoot().quietlyComplete()
    }

    actual fun helpComplete(maxTasks: Int) {
        if (maxTasks > 0 && status < 2) {
            val pool = currentThreadForkJoinPool() ?: submittedPool
            var remaining = maxTasks
            while (remaining > 0 && status < 2) {
                if (pool == null || !pool.tryHelpOneTask()) break
                remaining--
            }
        }
    }

    override fun recordExceptionalCompletion(ex: Throwable) {
        var a: CountedCompleter<*> = this
        var s: CountedCompleter<*> = a
        while (true) {
            val recorded = a.taskLock.withLock {
                if (a.status < 2) {
                    a.exception = ex
                    a.status = 4
                    a.doneCond.signalAll()
                    true
                } else {
                    false
                }
            }
            if (!recorded) break
            if (!a.onExceptionalCompletion(ex, s)) break
            s = a
            val p = a.completerRef ?: break
            a = p
        }
    }

    actual final override fun exec(): Boolean {
        compute()
        return false
    }

    @Suppress("UNCHECKED_CAST")
    actual override fun getRawResult(): T = null as T

    actual override fun setRawResult(t: T) {}
}

actual open class ForkJoinPool : AbstractExecutorService {
    private val targetParallelism: Int
    private val poolLock = ReentrantLock()
    private val workCond = poolLock.newCondition()
    private val termCond = poolLock.newCondition()
    private val taskQueue = ArrayDeque<ForkJoinTask<*>>()
    private val workers = ArrayList<Thread>()
    private var activeCount = 0
    private var stealCounter = 0L
    private var shutdownFlag = false
    private var terminatedFlag = false
    private val isCommonPoolInstance: Boolean

    actual constructor() : this(defaultParallelism(), isCommon = false)

    actual constructor(parallelism: Int) : this(parallelism, isCommon = false)

    private constructor(parallelism: Int, isCommon: Boolean) : super() {
        if (parallelism <= 0 || parallelism > 0x7fff) throw IllegalArgumentException()
        this.targetParallelism = parallelism
        this.isCommonPoolInstance = isCommon
    }

    internal fun enqueueTask(task: ForkJoinTask<*>) {
        poolLock.withLock {
            if (shutdownFlag) throw RejectedExecutionException("ForkJoinPool is shutdown")
            task.submittedPool = this
            taskQueue.addLast(task)
            ensureWorkerCountLocked()
            workCond.signal()
        }
    }

    internal fun removeQueuedTask(task: ForkJoinTask<*>): Boolean = poolLock.withLock {
        taskQueue.remove(task)
    }

    internal fun tryHelpOneTask(): Boolean {
        val nextTask = poolLock.withLock {
            if (taskQueue.isEmpty()) null else {
                stealCounter++
                taskQueue.removeLast()
            }
        } ?: return false
        nextTask.doExec()
        return true
    }

    private fun ensureWorkerCountLocked() {
        if (shutdownFlag) return
        val needed = minOf(targetParallelism, taskQueue.size + activeCount)
        while (workers.size < needed) {
            val worker = Thread(Runnable { workerLoop() })
            workers.add(worker)
            registerForkJoinWorker(worker, this)
            worker.start()
        }
    }

    private fun workerLoop() {
        try {
            while (true) {
                val task: ForkJoinTask<*>? = poolLock.withLock {
                    while (taskQueue.isEmpty() && !shutdownFlag) {
                        // Wait up to 200ms before retiring idle worker
                        val rem = workCond.awaitNanos(200_000_000L)
                        if (rem <= 0L && taskQueue.isEmpty()) {
                            workers.remove(Thread.currentThread())
                            if (shutdownFlag && workers.isEmpty() && taskQueue.isEmpty()) {
                                terminatedFlag = true
                                termCond.signalAll()
                            }
                            return
                        }
                    }
                    if (taskQueue.isEmpty() && shutdownFlag) {
                        workers.remove(Thread.currentThread())
                        if (workers.isEmpty()) {
                            terminatedFlag = true
                            termCond.signalAll()
                        }
                        return
                    }
                    activeCount++
                    taskQueue.removeFirst()
                }

                try {
                    task?.doExec()
                } finally {
                    poolLock.withLock {
                        activeCount--
                        if (taskQueue.isEmpty() && activeCount == 0) {
                            termCond.signalAll()
                        }
                    }
                }
            }
        } finally {
            unregisterForkJoinWorker(Thread.currentThread())
        }
    }

    actual open fun <T> invoke(task: ForkJoinTask<T>): T {
        if (shutdownFlag) throw RejectedExecutionException("ForkJoinPool is shutdown")
        task.submittedPool = this
        task.fork()
        return task.join()
    }

    actual open fun execute(task: ForkJoinTask<*>) {
        enqueueTask(task)
    }

    actual override fun execute(task: Runnable) {
        if (task is ForkJoinTask<*>) {
            enqueueTask(task)
        } else {
            enqueueTask(ForkJoinTask.adapt(task))
        }
    }

    actual open fun <T> submit(task: ForkJoinTask<T>): ForkJoinTask<T> {
        enqueueTask(task)
        return task
    }

    actual override fun <T> submit(task: Callable<T>): ForkJoinTask<T> {
        val fjt = ForkJoinTask.adapt(task)
        enqueueTask(fjt)
        return fjt
    }

    actual override fun <T> submit(task: Runnable, result: T): ForkJoinTask<T> {
        val fjt = ForkJoinTask.adapt(task, result)
        enqueueTask(fjt)
        return fjt
    }

    actual override fun submit(task: Runnable): ForkJoinTask<*> {
        val fjt = if (task is ForkJoinTask<*>) task else ForkJoinTask.adapt(task)
        enqueueTask(fjt)
        return fjt
    }

    actual override fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>> {
        val futures = ArrayList<Future<T>>(tasks.size)
        var done = false
        try {
            for (t in tasks) {
                val f = ForkJoinTask.adapt(t)
                futures.add(f)
                enqueueTask(f)
            }
            for (f in futures) {
                (f as ForkJoinTask<*>).quietlyJoin()
            }
            done = true
            return futures
        } finally {
            if (!done) {
                for (f in futures) f.cancel(true)
            }
        }
    }

    actual open fun getParallelism(): Int = targetParallelism

    actual open fun getPoolSize(): Int = poolLock.withLock { workers.size }

    actual open fun getAsyncMode(): Boolean = false

    actual open fun getRunningThreadCount(): Int = poolLock.withLock { workers.size }

    actual open fun getActiveThreadCount(): Int = poolLock.withLock { activeCount }

    actual open fun isQuiescent(): Boolean = poolLock.withLock { activeCount == 0 && taskQueue.isEmpty() }

    actual open fun getStealCount(): Long = poolLock.withLock { stealCounter }

    actual open fun getQueuedTaskCount(): Long = poolLock.withLock { taskQueue.size.toLong() }

    actual open fun getQueuedSubmissionCount(): Int = poolLock.withLock { taskQueue.size }

    actual open fun hasQueuedSubmissions(): Boolean = poolLock.withLock { taskQueue.isNotEmpty() }

    actual override fun shutdown() {
        if (isCommonPoolInstance) return
        poolLock.withLock {
            shutdownFlag = true
            if (workers.isEmpty() && taskQueue.isEmpty()) {
                terminatedFlag = true
            }
            workCond.signalAll()
            termCond.signalAll()
        }
    }

    actual override fun shutdownNow(): List<Runnable> {
        if (isCommonPoolInstance) return emptyList()
        poolLock.withLock {
            shutdownFlag = true
            while (taskQueue.isNotEmpty()) {
                val t = taskQueue.removeFirst()
                t.cancel(true)
            }
            if (workers.isEmpty()) {
                terminatedFlag = true
            }
            workCond.signalAll()
            termCond.signalAll()
        }
        return emptyList()
    }

    actual override fun isTerminated(): Boolean = poolLock.withLock { terminatedFlag }

    actual open fun isTerminating(): Boolean = poolLock.withLock { shutdownFlag && !terminatedFlag }

    actual override fun isShutdown(): Boolean = poolLock.withLock { shutdownFlag }

    actual override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        if (isCommonPoolInstance) {
            return awaitQuiescence(timeout, unit)
        }
        var nanos = unit.toNanos(timeout)
        return poolLock.withLock {
            while (!terminatedFlag) {
                if (nanos <= 0L) return@withLock false
                nanos = termCond.awaitNanos(nanos)
            }
            true
        }
    }

    actual open fun awaitQuiescence(timeout: Long, unit: TimeUnit): Boolean {
        val deadline = System.nanoTime() + unit.toNanos(timeout)
        while (!isQuiescent()) {
            if (tryHelpOneTask()) continue
            val rem = deadline - System.nanoTime()
            if (rem <= 0L) return isQuiescent()
            poolLock.withLock {
                if (!isQuiescent()) {
                    termCond.awaitNanos(minOf(rem, 10_000_000L))
                }
            }
        }
        return true
    }

    actual interface ManagedBlocker {
        actual fun block(): Boolean
        actual fun isReleasable(): Boolean
    }

    actual companion object {
        private val commonInstance: ForkJoinPool by lazy {
            ForkJoinPool(defaultParallelism(), isCommon = true)
        }

        private fun defaultParallelism(): Int = 4

        actual fun commonPool(): ForkJoinPool = commonInstance

        actual fun getCommonPoolParallelism(): Int = commonInstance.getParallelism()

        actual fun managedBlock(blocker: ManagedBlocker) {
            while (!blocker.isReleasable()) {
                if (blocker.block()) break
            }
        }
    }
}
