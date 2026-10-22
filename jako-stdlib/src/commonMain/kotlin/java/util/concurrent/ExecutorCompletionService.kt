@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable

expect open class ExecutorCompletionService<V> : CompletionService<V> {
    constructor(executor: Executor)
    constructor(executor: Executor, completionQueue: BlockingQueue<Future<V>>)

    override fun submit(task: Callable<V>): Future<V>
    override fun submit(task: Runnable, result: V): Future<V>
    override fun take(): Future<V>
    override fun poll(): Future<V>?
    override fun poll(timeout: Long, unit: TimeUnit): Future<V>?
}
