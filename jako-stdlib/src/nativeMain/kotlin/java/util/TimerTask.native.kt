package java.util

import java.lang.Runnable
import java.util.concurrent.locks.ReentrantLock

actual abstract class TimerTask : Runnable {
    private val lock = ReentrantLock()
    private var scheduled = false
    private var cancelled = false
    private var lastExecutionTime = 0L

    actual constructor()

    actual abstract override fun run()

    actual open fun cancel(): Boolean {
        lock.lock()
        try {
            val result = scheduled && !cancelled
            cancelled = true
            return result
        } finally {
            lock.unlock()
        }
    }

    actual open fun scheduledExecutionTime(): Long {
        lock.lock()
        try {
            return lastExecutionTime
        } finally {
            lock.unlock()
        }
    }

    internal fun markScheduled(): Boolean {
        lock.lock()
        try {
            if (scheduled || cancelled) return false
            scheduled = true
            return true
        } finally {
            lock.unlock()
        }
    }

    internal fun isCancelled(): Boolean {
        lock.lock()
        try {
            return cancelled
        } finally {
            lock.unlock()
        }
    }

    internal fun recordExecution(time: Long) {
        lock.lock()
        try {
            lastExecutionTime = time
        } finally {
            lock.unlock()
        }
    }
}
