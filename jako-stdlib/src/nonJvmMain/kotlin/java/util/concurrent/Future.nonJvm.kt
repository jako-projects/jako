package java.util.concurrent

actual interface Future<V> {
    actual fun cancel(mayInterruptIfRunning: Boolean): Boolean
    actual fun isCancelled(): Boolean
    actual fun isDone(): Boolean
    actual val isCancelled: Boolean
    actual val isDone: Boolean
    actual fun get(): V
    actual fun get(timeout: Long, unit: TimeUnit): V
}
