package java.util.concurrent.locks

import java.util.concurrent.TimeUnit

actual interface Condition {
    actual fun await()
    actual fun awaitUninterruptibly()
    actual fun awaitNanos(nanosTimeout: Long): Long
    actual fun await(time: Long, unit: TimeUnit): Boolean
    actual fun signal()
    actual fun signalAll()
}
