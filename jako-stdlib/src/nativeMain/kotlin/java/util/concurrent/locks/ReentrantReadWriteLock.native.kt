package java.util.concurrent.locks

import java.io.Serializable
import java.lang.IllegalMonitorStateException
import java.lang.Thread
import java.util.concurrent.TimeUnit
import kotlin.concurrent.withLock

actual open class ReentrantReadWriteLock actual constructor(
    private val fair: Boolean
) : ReadWriteLock, Serializable {
    actual constructor() : this(false)

    private val lock = ReentrantLock(fair)
    private val condition = lock.newCondition()
    private var readers: Int = 0
    private val readHolds = mutableMapOf<Thread, Int>()
    private var writers: Int = 0
    private var writeHoldCount: Int = 0
    private var writerThread: Thread? = null

    private val readerLock = ReadLock(this)
    private val writerLock = WriteLock(this)

    actual override fun readLock(): ReadLock = readerLock
    actual override fun writeLock(): WriteLock = writerLock
    actual open fun isFair(): Boolean = fair
    actual open fun getReadLockCount(): Int = lock.withLock { readers }
    actual open fun isWriteLocked(): Boolean = lock.withLock { writers > 0 }
    actual open fun isWriteLockedByCurrentThread(): Boolean = lock.withLock {
        writers > 0 && writerThread === Thread.currentThread()
    }
    actual open fun getWriteHoldCount(): Int = lock.withLock {
        if (writers > 0 && writerThread === Thread.currentThread()) writeHoldCount else 0
    }
    actual open fun getReadHoldCount(): Int = lock.withLock {
        readHolds[Thread.currentThread()] ?: 0
    }

    private fun canAcquireRead(current: Thread): Boolean {
        return writers == 0 || writerThread === current
    }

    private fun recordReadAcquire(current: Thread) {
        readers++
        readHolds[current] = (readHolds[current] ?: 0) + 1
    }

    actual open class ReadLock internal constructor(private val rwLock: ReentrantReadWriteLock) : Lock, Serializable {
        actual override fun lock() {
            val current = Thread.currentThread()
            rwLock.lock.withLock {
                while (!rwLock.canAcquireRead(current)) {
                    rwLock.condition.await()
                }
                rwLock.recordReadAcquire(current)
            }
        }

        actual override fun lockInterruptibly() = lock()

        actual override fun tryLock(): Boolean {
            val current = Thread.currentThread()
            return rwLock.lock.withLock {
                if (!rwLock.canAcquireRead(current)) return@withLock false
                rwLock.recordReadAcquire(current)
                true
            }
        }

        actual override fun tryLock(time: Long, unit: TimeUnit): Boolean {
            var nanos = unit.toNanos(time)
            val current = Thread.currentThread()
            return rwLock.lock.withLock {
                while (!rwLock.canAcquireRead(current)) {
                    if (nanos <= 0L) return@withLock false
                    nanos = rwLock.condition.awaitNanos(nanos)
                }
                rwLock.recordReadAcquire(current)
                true
            }
        }

        actual override fun unlock() {
            val current = Thread.currentThread()
            rwLock.lock.withLock {
                val held = rwLock.readHolds[current] ?: 0
                if (held <= 0) throw IllegalMonitorStateException()
                if (held == 1) {
                    rwLock.readHolds.remove(current)
                } else {
                    rwLock.readHolds[current] = held - 1
                }
                rwLock.readers--
                if (rwLock.readers == 0) {
                    rwLock.condition.signalAll()
                }
            }
        }

        actual override fun newCondition(): Condition = throw UnsupportedOperationException()
    }

    actual open class WriteLock internal constructor(private val rwLock: ReentrantReadWriteLock) : Lock, Serializable {
        actual override fun lock() {
            val current = Thread.currentThread()
            rwLock.lock.withLock {
                if (rwLock.writers > 0 && rwLock.writerThread === current) {
                    rwLock.writeHoldCount++
                    return
                }
                while (rwLock.readers > 0 || rwLock.writers > 0) {
                    rwLock.condition.await()
                }
                rwLock.writers = 1
                rwLock.writeHoldCount = 1
                rwLock.writerThread = current
            }
        }

        actual override fun lockInterruptibly() = lock()

        actual override fun tryLock(): Boolean {
            val current = Thread.currentThread()
            return rwLock.lock.withLock {
                if (rwLock.writers > 0 && rwLock.writerThread === current) {
                    rwLock.writeHoldCount++
                    return@withLock true
                }
                if (rwLock.readers > 0 || rwLock.writers > 0) return@withLock false
                rwLock.writers = 1
                rwLock.writeHoldCount = 1
                rwLock.writerThread = current
                true
            }
        }

        actual override fun tryLock(time: Long, unit: TimeUnit): Boolean {
            var nanos = unit.toNanos(time)
            val current = Thread.currentThread()
            return rwLock.lock.withLock {
                if (rwLock.writers > 0 && rwLock.writerThread === current) {
                    rwLock.writeHoldCount++
                    return@withLock true
                }
                while (rwLock.readers > 0 || rwLock.writers > 0) {
                    if (nanos <= 0L) return@withLock false
                    nanos = rwLock.condition.awaitNanos(nanos)
                    if (rwLock.writers > 0 && rwLock.writerThread === current) {
                        rwLock.writeHoldCount++
                        return@withLock true
                    }
                }
                rwLock.writers = 1
                rwLock.writeHoldCount = 1
                rwLock.writerThread = current
                true
            }
        }

        actual override fun unlock() {
            val current = Thread.currentThread()
            rwLock.lock.withLock {
                if (rwLock.writers == 0 || rwLock.writerThread !== current) {
                    throw IllegalMonitorStateException()
                }
                rwLock.writeHoldCount--
                if (rwLock.writeHoldCount == 0) {
                    rwLock.writers = 0
                    rwLock.writerThread = null
                    rwLock.condition.signalAll()
                }
            }
        }

        actual override fun newCondition(): Condition {
            val innerCondition = rwLock.lock.newCondition()
            return object : Condition {
                private fun <T> withWriteRelease(block: () -> T): T {
                    val current = Thread.currentThread()
                    return rwLock.lock.withLock {
                        if (rwLock.writers == 0 || rwLock.writerThread !== current) {
                            throw IllegalMonitorStateException()
                        }
                        val savedHolds = rwLock.writeHoldCount
                        rwLock.writeHoldCount = 0
                        rwLock.writers = 0
                        rwLock.writerThread = null
                        rwLock.condition.signalAll()
                        try {
                            block()
                        } finally {
                            while (rwLock.readers > 0 || rwLock.writers > 0) {
                                rwLock.condition.await()
                            }
                            rwLock.writers = 1
                            rwLock.writeHoldCount = savedHolds
                            rwLock.writerThread = current
                        }
                    }
                }

                override fun await() = withWriteRelease { innerCondition.await() }
                override fun awaitUninterruptibly() = withWriteRelease { innerCondition.awaitUninterruptibly() }
                override fun awaitNanos(nanosTimeout: Long): Long = withWriteRelease { innerCondition.awaitNanos(nanosTimeout) }
                override fun await(time: Long, unit: TimeUnit): Boolean = withWriteRelease { innerCondition.await(time, unit) }

                override fun signal() {
                    val current = Thread.currentThread()
                    rwLock.lock.withLock {
                        if (rwLock.writers == 0 || rwLock.writerThread !== current) {
                            throw IllegalMonitorStateException()
                        }
                        innerCondition.signal()
                    }
                }

                override fun signalAll() {
                    val current = Thread.currentThread()
                    rwLock.lock.withLock {
                        if (rwLock.writers == 0 || rwLock.writerThread !== current) {
                            throw IllegalMonitorStateException()
                        }
                        innerCondition.signalAll()
                    }
                }
            }
        }
        actual open fun isHeldByCurrentThread(): Boolean = rwLock.isWriteLockedByCurrentThread()
        actual open fun getHoldCount(): Int = rwLock.getWriteHoldCount()
    }
}
