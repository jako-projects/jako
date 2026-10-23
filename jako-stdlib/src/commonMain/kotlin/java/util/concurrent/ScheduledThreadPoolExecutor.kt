@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable

expect open class ScheduledThreadPoolExecutor : ThreadPoolExecutor, ScheduledExecutorService {
    constructor(corePoolSize: Int)
    constructor(corePoolSize: Int, threadFactory: ThreadFactory)

    override fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*>
    override fun <V> schedule(callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledFuture<V>
    override fun scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture<*>
    override fun scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture<*>
}
