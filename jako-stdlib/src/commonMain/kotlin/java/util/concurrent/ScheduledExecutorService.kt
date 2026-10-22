@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable

expect interface ScheduledExecutorService : ExecutorService {
    fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*>
    fun <V> schedule(callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledFuture<V>
    fun scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture<*>
    fun scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture<*>
}
