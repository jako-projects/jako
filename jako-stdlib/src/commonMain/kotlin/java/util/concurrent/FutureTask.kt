@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable

expect open class FutureTask<V> : RunnableFuture<V> {
    constructor(callable: Callable<V>)
    constructor(runnable: Runnable, result: V)

    override fun run()
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean
    override fun isCancelled(): Boolean
    override fun isDone(): Boolean
    override val isCancelled: Boolean
    override val isDone: Boolean
    override fun get(): V
    override fun get(timeout: Long, unit: TimeUnit): V
}
