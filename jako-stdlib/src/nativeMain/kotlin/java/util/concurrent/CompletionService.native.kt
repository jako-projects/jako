package java.util.concurrent

import java.lang.Runnable
import kotlin.concurrent.withLock

actual interface CompletionService<V> {
    actual fun submit(task: Callable<V>): Future<V>
    actual fun submit(task: Runnable, result: V): Future<V>
    actual fun take(): Future<V>
    actual fun poll(): Future<V>?
    actual fun poll(timeout: Long, unit: TimeUnit): Future<V>?
}
