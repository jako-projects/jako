@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.locks

import java.util.concurrent.TimeUnit

expect interface Lock {
    fun lock()
    fun lockInterruptibly()
    fun tryLock(): Boolean
    fun tryLock(time: Long, unit: TimeUnit): Boolean
    fun unlock()
    fun newCondition(): Condition
}
