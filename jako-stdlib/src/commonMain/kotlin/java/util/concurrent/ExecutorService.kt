@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable

expect interface ExecutorService : Executor {
    fun shutdown()
    fun shutdownNow(): List<Runnable>
    fun isShutdown(): Boolean
    fun isTerminated(): Boolean
    fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean
    fun <T> submit(task: Callable<T>): Future<T>
    fun <T> submit(task: Runnable, result: T): Future<T>
    fun submit(task: Runnable): Future<*>
    fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>>
    fun <T> invokeAll(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): List<Future<T>>
    fun <T> invokeAny(tasks: Collection<Callable<T>>): T
    fun <T> invokeAny(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): T
}
