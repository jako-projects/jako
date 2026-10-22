package java.util.concurrent

import kotlin.concurrent.withLock

actual interface ScheduledFuture<V> : Delayed, Future<V>
