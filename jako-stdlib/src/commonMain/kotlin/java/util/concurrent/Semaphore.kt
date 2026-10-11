@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable

expect open class Semaphore : Serializable {
    constructor(permits: Int)
    constructor(permits: Int, fair: Boolean)

    open fun acquire()
    open fun acquire(permits: Int)
    open fun acquireUninterruptibly()
    open fun acquireUninterruptibly(permits: Int)
    open fun tryAcquire(): Boolean
    open fun tryAcquire(permits: Int): Boolean
    open fun tryAcquire(timeout: Long, unit: TimeUnit): Boolean
    open fun tryAcquire(permits: Int, timeout: Long, unit: TimeUnit): Boolean
    open fun release()
    open fun release(permits: Int)
    open fun availablePermits(): Int
    open fun drainPermits(): Int
    open fun isFair(): Boolean
}
