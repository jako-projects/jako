package java.util.concurrent

import com.bennyhuo.jako.WebTaskScheduler
import java.lang.Runnable
import java.lang.System
import java.lang.Thread
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal fun registerForkJoinWorker(thread: Thread, pool: ForkJoinPool) {
    poolThreadOwnerLock.withLock { threadToPool[thread] = pool }
}

internal fun unregisterForkJoinWorker(thread: Thread) {
    poolThreadOwnerLock.withLock { threadToPool.remove(thread) }
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
        val current = Thread.currentThread()
        registerForkJoinWorker(current, this)
        try {
            nextTask.doExec()
        } finally {
            unregisterForkJoinWorker(current)
            poolLock.withLock {
                if (shutdownFlag && workers.isEmpty() && taskQueue.isEmpty() && activeCount == 0) {
                    terminatedFlag = true
                    termCond.signalAll()
                }
            }
        }
        return true
    }

    private fun ensureWorkerCountLocked() {
        if (shutdownFlag) return
        if (!WebTaskScheduler.isMultiThreadingSupported()) {
            WebTaskScheduler.schedule(0L) {
                tryHelpOneTask()
            }
            return
        }
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
