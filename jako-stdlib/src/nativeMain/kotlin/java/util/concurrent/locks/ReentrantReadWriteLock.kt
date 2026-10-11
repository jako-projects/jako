package java.util.concurrent.locks

import java.io.Serializable
import java.util.concurrent.TimeUnit

actual open class ReentrantReadWriteLock actual constructor(
    private val fair: Boolean
) : ReadWriteLock, Serializable {
    actual constructor() : this(false)

    private val lock = ReentrantLock(fair)
    private val condition = lock.newCondition()
    private var readers: Int = 0
    private var writers: Int = 0
    private var writeHoldCount: Int = 0

    private val readerLock = ReadLock(this)
    private val writerLock = WriteLock(this)

    actual override fun readLock(): ReadLock = readerLock
    actual override fun writeLock(): WriteLock = writerLock
    actual open fun isFair(): Boolean = fair
    actual open fun getReadLockCount(): Int = readers
    actual open fun isWriteLocked(): Boolean = writers > 0
    actual open fun isWriteLockedByCurrentThread(): Boolean = lock.isHeldByCurrentThread && writers > 0
    actual open fun getWriteHoldCount(): Int = if (isWriteLockedByCurrentThread()) writeHoldCount else 0
    actual open fun getReadHoldCount(): Int = 0

    actual open class ReadLock internal constructor(private val rwLock: ReentrantReadWriteLock) : Lock, Serializable {
        actual override fun lock() {
            rwLock.lock.lock()
            try {
                while (rwLock.writers > 0) {
                    rwLock.condition.await()
                }
                rwLock.readers++
            } finally {
                rwLock.lock.unlock()
            }
        }

        actual override fun lockInterruptibly() = lock()

        actual override fun tryLock(): Boolean {
            rwLock.lock.lock()
            try {
                if (rwLock.writers > 0) return false
                rwLock.readers++
                return true
            } finally {
                rwLock.lock.unlock()
            }
        }

        actual override fun tryLock(time: Long, unit: TimeUnit): Boolean {
            val nanos = unit.toNanos(time)
            rwLock.lock.lock()
            try {
                if (rwLock.writers > 0) {
                    if (nanos <= 0) return false
                    rwLock.condition.awaitNanos(nanos)
                    if (rwLock.writers > 0) return false
                }
                rwLock.readers++
                return true
            } finally {
                rwLock.lock.unlock()
            }
        }

        actual override fun unlock() {
            rwLock.lock.lock()
            try {
                if (rwLock.readers > 0) {
                    rwLock.readers--
                    if (rwLock.readers == 0) {
                        rwLock.condition.signalAll()
                    }
                }
            } finally {
                rwLock.lock.unlock()
            }
        }

        actual override fun newCondition(): Condition = throw UnsupportedOperationException()
    }

    actual open class WriteLock internal constructor(private val rwLock: ReentrantReadWriteLock) : Lock, Serializable {
        actual override fun lock() {
            rwLock.lock.lock()
            try {
                while (rwLock.readers > 0 || rwLock.writers > 0) {
                    rwLock.condition.await()
                }
                rwLock.writers = 1
                rwLock.writeHoldCount = 1
            } finally {
                rwLock.lock.unlock()
            }
        }

        actual override fun lockInterruptibly() = lock()

        actual override fun tryLock(): Boolean {
            rwLock.lock.lock()
            try {
                if (rwLock.readers > 0 || rwLock.writers > 0) return false
                rwLock.writers = 1
                rwLock.writeHoldCount = 1
                return true
            } finally {
                rwLock.lock.unlock()
            }
        }

        actual override fun tryLock(time: Long, unit: TimeUnit): Boolean {
            val nanos = unit.toNanos(time)
            rwLock.lock.lock()
            try {
                if (rwLock.readers > 0 || rwLock.writers > 0) {
                    if (nanos <= 0) return false
                    rwLock.condition.awaitNanos(nanos)
                    if (rwLock.readers > 0 || rwLock.writers > 0) return false
                }
                rwLock.writers = 1
                rwLock.writeHoldCount = 1
                return true
            } finally {
                rwLock.lock.unlock()
            }
        }

        actual override fun unlock() {
            rwLock.lock.lock()
            try {
                rwLock.writers = 0
                rwLock.writeHoldCount = 0
                rwLock.condition.signalAll()
            } finally {
                rwLock.lock.unlock()
            }
        }

        actual override fun newCondition(): Condition = rwLock.lock.newCondition()
        actual open fun isHeldByCurrentThread(): Boolean = rwLock.isWriteLockedByCurrentThread()
        actual open fun getHoldCount(): Int = rwLock.getWriteHoldCount()
    }
}
