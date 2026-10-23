@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable

expect open class ThreadPoolExecutor : AbstractExecutorService {
    constructor(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit,
        workQueue: BlockingQueue<Runnable>,
        threadFactory: ThreadFactory
    )

    override fun execute(command: Runnable)
    override fun shutdown()
    override fun shutdownNow(): List<Runnable>
    override fun isShutdown(): Boolean
    override fun isTerminated(): Boolean
    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean
    override fun <T> submit(task: Callable<T>): Future<T>
    override fun <T> submit(task: Runnable, result: T): Future<T>
    override fun submit(task: Runnable): Future<*>
}
