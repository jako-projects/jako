package java.util.concurrent

import java.io.Serializable
import java.util.concurrent.locks.ReentrantLock

actual open class Semaphore : Serializable {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private var permits: Int
    private val fair: Boolean

    actual constructor(permits: Int) : this(permits, false)

    actual constructor(permits: Int, fair: Boolean) {
        this.permits = permits
        this.fair = fair
    }

    actual open fun acquire() = acquire(1)

    actual open fun acquire(permits: Int) {
        if (permits < 0) throw IllegalArgumentException()
        lock.lock()
        try {
            while (this.permits < permits) {
                condition.await()
            }
            this.permits -= permits
        } finally {
            lock.unlock()
        }
    }

    actual open fun acquireUninterruptibly() = acquire(1)
    actual open fun acquireUninterruptibly(permits: Int) = acquire(permits)

    actual open fun tryAcquire(): Boolean = tryAcquire(1)

    actual open fun tryAcquire(permits: Int): Boolean {
        if (permits < 0) throw IllegalArgumentException()
        lock.lock()
        try {
            if (this.permits >= permits) {
                this.permits -= permits
                return true
            }
            return false
        } finally {
            lock.unlock()
        }
    }

    actual open fun tryAcquire(timeout: Long, unit: TimeUnit): Boolean = tryAcquire(1, timeout, unit)

    actual open fun tryAcquire(permits: Int, timeout: Long, unit: TimeUnit): Boolean {
        if (permits < 0) throw IllegalArgumentException()
        var nanos = unit.toNanos(timeout)
        lock.lock()
        try {
            while (this.permits < permits) {
                if (nanos <= 0) return false
                nanos = condition.awaitNanos(nanos)
            }
            this.permits -= permits
            return true
        } finally {
            lock.unlock()
        }
    }

    actual open fun release() = release(1)

    actual open fun release(permits: Int) {
        if (permits < 0) throw IllegalArgumentException()
        lock.lock()
        try {
            this.permits += permits
            condition.signalAll()
        } finally {
            lock.unlock()
        }
    }

    actual open fun availablePermits(): Int {
        lock.lock()
        try {
            return permits
        } finally {
            lock.unlock()
        }
    }

    actual open fun drainPermits(): Int {
        lock.lock()
        try {
            val d = permits
            permits = 0
            return d
        } finally {
            lock.unlock()
        }
    }

    actual open fun isFair(): Boolean = fair
}
