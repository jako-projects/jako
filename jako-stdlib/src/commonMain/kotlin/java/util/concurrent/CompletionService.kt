@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable

/**
 * [Nullability Decision] CompletionService.poll returns Future<V>? (null when no task has completed yet).
 */
expect interface CompletionService<V> {
    fun submit(task: Callable<V>): Future<V>
    fun submit(task: Runnable, result: V): Future<V>
    fun take(): Future<V>
    fun poll(): Future<V>?
    fun poll(timeout: Long, unit: TimeUnit): Future<V>?
}
