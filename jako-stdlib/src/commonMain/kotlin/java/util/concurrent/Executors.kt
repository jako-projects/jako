@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable

expect object Executors {
    fun newSingleThreadExecutor(): ExecutorService
    fun newSingleThreadExecutor(threadFactory: ThreadFactory): ExecutorService
    fun newFixedThreadPool(nThreads: Int): ExecutorService
    fun newFixedThreadPool(nThreads: Int, threadFactory: ThreadFactory): ExecutorService
    fun newCachedThreadPool(): ExecutorService
    fun newCachedThreadPool(threadFactory: ThreadFactory): ExecutorService
    fun newSingleThreadScheduledExecutor(): ScheduledExecutorService
    fun newSingleThreadScheduledExecutor(threadFactory: ThreadFactory): ScheduledExecutorService
    fun newScheduledThreadPool(corePoolSize: Int): ScheduledExecutorService
    fun newScheduledThreadPool(corePoolSize: Int, threadFactory: ThreadFactory): ScheduledExecutorService
    fun newWorkStealingPool(parallelism: Int): ExecutorService
    fun newWorkStealingPool(): ExecutorService
    fun unconfigurableExecutorService(executor: ExecutorService): ExecutorService
    fun unconfigurableScheduledExecutorService(executor: ScheduledExecutorService): ScheduledExecutorService
    fun defaultThreadFactory(): ThreadFactory
    fun <T> callable(task: Runnable, result: T): Callable<T>
    fun callable(task: Runnable): Callable<Any?>
}
