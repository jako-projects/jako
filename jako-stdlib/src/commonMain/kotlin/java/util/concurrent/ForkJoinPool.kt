@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable

expect open class ForkJoinPool : AbstractExecutorService {
    constructor()
    constructor(parallelism: Int)

    open fun <T> invoke(task: ForkJoinTask<T>): T
    open fun execute(task: ForkJoinTask<*>)
    override fun execute(task: Runnable)
    open fun <T> submit(task: ForkJoinTask<T>): ForkJoinTask<T>
    override fun <T> submit(task: Callable<T>): ForkJoinTask<T>
    override fun <T> submit(task: Runnable, result: T): ForkJoinTask<T>
    override fun submit(task: Runnable): ForkJoinTask<*>
    override fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>>
    open fun getParallelism(): Int
    open fun getPoolSize(): Int
    open fun getAsyncMode(): Boolean
    open fun getRunningThreadCount(): Int
    open fun getActiveThreadCount(): Int
    open fun isQuiescent(): Boolean
    open fun getStealCount(): Long
    open fun getQueuedTaskCount(): Long
    open fun getQueuedSubmissionCount(): Int
    open fun hasQueuedSubmissions(): Boolean
    override fun shutdown()
    override fun shutdownNow(): List<Runnable>
    override fun isTerminated(): Boolean
    open fun isTerminating(): Boolean
    override fun isShutdown(): Boolean
    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean
    open fun awaitQuiescence(timeout: Long, unit: TimeUnit): Boolean

    interface ManagedBlocker {
        fun block(): Boolean
        fun isReleasable(): Boolean
    }

    companion object {
        fun commonPool(): ForkJoinPool
        fun getCommonPoolParallelism(): Int
        fun managedBlock(blocker: ManagedBlocker)
    }
}
