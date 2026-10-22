package java.util.concurrent

import java.lang.Runnable
import kotlin.concurrent.withLock

actual interface ExecutorService : Executor {
    actual fun shutdown()
    actual fun shutdownNow(): List<Runnable>
    actual fun isShutdown(): Boolean
    actual fun isTerminated(): Boolean
    actual fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean
    actual fun <T> submit(task: Callable<T>): Future<T>
    actual fun <T> submit(task: Runnable, result: T): Future<T>
    actual fun submit(task: Runnable): Future<*>
    actual fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>>
    actual fun <T> invokeAll(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): List<Future<T>>
    actual fun <T> invokeAny(tasks: Collection<Callable<T>>): T
    actual fun <T> invokeAny(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): T
}
