package java.util.concurrent

import java.lang.Runnable
import kotlin.concurrent.withLock

actual interface ScheduledExecutorService : ExecutorService {
    actual fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*>
    actual fun <V> schedule(callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledFuture<V>
    actual fun scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture<*>
    actual fun scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture<*>
}
