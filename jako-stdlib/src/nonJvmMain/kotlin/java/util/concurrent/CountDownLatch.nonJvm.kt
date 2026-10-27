package java.util.concurrent

import java.util.concurrent.locks.ReentrantLock

actual open class CountDownLatch actual constructor(count: Int) {
    private val lock = ReentrantLock()
    private val cond = lock.newCondition()
    private var countVal = count

    init {
        require(count >= 0) { "count < 0" }
    }

    actual open fun await() {
        lock.lock()
        try {
            while (countVal > 0) {
                cond.await()
            }
        } finally {
            lock.unlock()
        }
    }

    actual open fun await(timeout: Long, unit: TimeUnit): Boolean {
        var nanos = unit.toNanos(timeout)
        lock.lock()
        try {
            while (countVal > 0) {
                if (nanos <= 0L) {
                    return false
                }
                nanos = cond.awaitNanos(nanos)
            }
            return true
        } finally {
            lock.unlock()
        }
    }

    actual open fun countDown() {
        lock.lock()
        try {
            if (countVal > 0) {
                countVal--
                if (countVal == 0) {
                    cond.signalAll()
                }
            }
        } finally {
            lock.unlock()
        }
    }

    actual open fun getCount(): Long {
        lock.lock()
        try {
            return countVal.toLong()
        } finally {
            lock.unlock()
        }
    }
}

