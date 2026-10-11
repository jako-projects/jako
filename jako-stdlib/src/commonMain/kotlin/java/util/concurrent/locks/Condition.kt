@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.locks

import java.util.concurrent.TimeUnit

expect interface Condition {
    fun await()
    fun awaitUninterruptibly()
    fun awaitNanos(nanosTimeout: Long): Long
    fun await(time: Long, unit: TimeUnit): Boolean
    fun signal()
    fun signalAll()
}
