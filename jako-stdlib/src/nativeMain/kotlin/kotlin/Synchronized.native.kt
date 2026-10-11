package kotlin

import java.lang.getObjectMonitor

actual inline fun <R> synchronized(lock: Any, block: () -> R): R {
    val monitor = getObjectMonitor(lock)
    monitor.lock.lock()
    try {
        return block()
    } finally {
        monitor.lock.unlock()
    }
}
