@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.locks

import java.io.Serializable
import java.lang.Thread
import java.util.concurrent.TimeUnit

/**
 * [Nullability Decision] Java LockSupport.unpark(Thread), park(Object), and getBlocker(Thread)
 * accept/return nullable references.
 */
expect object LockSupport {
    fun unpark(thread: Thread?)
    fun park(blocker: Any?)
    fun parkNanos(blocker: Any?, nanos: Long)
    fun parkUntil(blocker: Any?, deadline: Long)
    fun getBlocker(t: Thread?): Any?
    fun park()
    fun parkNanos(nanos: Long)
    fun parkUntil(deadline: Long)
}

/**
 * [Nullability Decision] Java StampedLock view methods (asReadLock, asWriteLock, asReadWriteLock)
 * return non-null Lock / ReadWriteLock views.
 */
expect open class StampedLock() : Serializable {
    open fun writeLock(): Long
    open fun tryWriteLock(): Long
    open fun tryWriteLock(time: Long, unit: TimeUnit): Long
    open fun writeLockInterruptibly(): Long
    open fun readLock(): Long
    open fun tryReadLock(): Long
    open fun tryReadLock(time: Long, unit: TimeUnit): Long
    open fun readLockInterruptibly(): Long
    open fun tryOptimisticRead(): Long
    open fun validate(stamp: Long): Boolean
    open fun unlockWrite(stamp: Long)
    open fun unlockRead(stamp: Long)
    open fun unlock(stamp: Long)
    open fun tryConvertToWriteLock(stamp: Long): Long
    open fun tryConvertToReadLock(stamp: Long): Long
    open fun tryConvertToOptimisticRead(stamp: Long): Long
    open fun tryUnlockWrite(): Boolean
    open fun tryUnlockRead(): Boolean
    open fun isWriteLocked(): Boolean
    open fun isReadLocked(): Boolean
    open fun getReadLockCount(): Int
    open fun asReadLock(): Lock
    open fun asWriteLock(): Lock
    open fun asReadWriteLock(): ReadWriteLock
    override fun toString(): String

    open val isWriteLocked: Boolean
    open val isReadLocked: Boolean
    open val readLockCount: Int
}
