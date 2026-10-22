@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable
import java.lang.Runnable

expect abstract class ForkJoinTask<V>() : Future<V>, Serializable {
    fun fork(): ForkJoinTask<V>
    fun join(): V
    fun invoke(): V
    open override fun cancel(mayInterruptIfRunning: Boolean): Boolean
    final override fun isDone(): Boolean
    final override fun isCancelled(): Boolean
    override val isDone: Boolean
    override val isCancelled: Boolean
    fun isCompletedAbnormally(): Boolean
    fun isCompletedNormally(): Boolean
    fun getException(): Throwable?
    open fun completeExceptionally(ex: Throwable)
    open fun complete(value: V)
    fun quietlyComplete()
    final override fun get(): V
    final override fun get(timeout: Long, unit: TimeUnit): V
    fun quietlyJoin()
    fun quietlyInvoke()
    open fun reinitialize()
    open fun tryUnfork(): Boolean
    abstract fun getRawResult(): V
    protected abstract fun setRawResult(value: V)
    protected abstract fun exec(): Boolean
    fun getForkJoinTaskTag(): Short
    fun setForkJoinTaskTag(newValue: Short): Short
    fun compareAndSetForkJoinTaskTag(expect: Short, update: Short): Boolean

    companion object {
        fun invokeAll(t1: ForkJoinTask<*>, t2: ForkJoinTask<*>)
        fun invokeAll(vararg tasks: ForkJoinTask<*>)
        fun <T : ForkJoinTask<*>> invokeAll(tasks: Collection<T>): Collection<T>
        fun helpQuiesce()
        fun getPool(): ForkJoinPool?
        fun inForkJoinPool(): Boolean
        fun getQueuedTaskCount(): Int
        fun getSurplusQueuedTaskCount(): Int
        fun adapt(runnable: Runnable): ForkJoinTask<*>
        fun <T> adapt(runnable: Runnable, result: T): ForkJoinTask<T>
        fun <T> adapt(callable: Callable<out T>): ForkJoinTask<T>
    }
}
