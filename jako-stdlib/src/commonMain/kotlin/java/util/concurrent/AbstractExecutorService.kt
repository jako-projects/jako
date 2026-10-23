@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable

expect abstract class AbstractExecutorService() : ExecutorService {
    override fun <T> submit(task: Callable<T>): Future<T>
    override fun <T> submit(task: Runnable, result: T): Future<T>
    override fun submit(task: Runnable): Future<*>
    override fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>>
    override fun <T> invokeAll(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): List<Future<T>>
    override fun <T> invokeAny(tasks: Collection<Callable<T>>): T
    override fun <T> invokeAny(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): T
}
