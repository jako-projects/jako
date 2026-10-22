@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect interface Future<V> {
    fun cancel(mayInterruptIfRunning: Boolean): Boolean
    fun isCancelled(): Boolean
    fun isDone(): Boolean
    val isCancelled: Boolean
    val isDone: Boolean
    fun get(): V
    fun get(timeout: Long, unit: TimeUnit): V
}
