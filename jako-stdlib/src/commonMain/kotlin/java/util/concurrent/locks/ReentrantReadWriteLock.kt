@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.locks

import java.io.Serializable
import java.util.concurrent.TimeUnit

expect open class ReentrantReadWriteLock : ReadWriteLock, Serializable {
    constructor()
    constructor(fair: Boolean)

    override fun readLock(): ReadLock
    override fun writeLock(): WriteLock
    open fun isFair(): Boolean
    open fun getReadLockCount(): Int
    open fun isWriteLocked(): Boolean
    open fun isWriteLockedByCurrentThread(): Boolean
    open fun getWriteHoldCount(): Int
    open fun getReadHoldCount(): Int

    open class ReadLock : Lock, Serializable {
        override fun lock()
        override fun lockInterruptibly()
        override fun tryLock(): Boolean
        override fun tryLock(time: Long, unit: TimeUnit): Boolean
        override fun unlock()
        override fun newCondition(): Condition
    }

    open class WriteLock : Lock, Serializable {
        override fun lock()
        override fun lockInterruptibly()
        override fun tryLock(): Boolean
        override fun tryLock(time: Long, unit: TimeUnit): Boolean
        override fun unlock()
        override fun newCondition(): Condition
        open fun isHeldByCurrentThread(): Boolean
        open fun getHoldCount(): Int
    }
}
