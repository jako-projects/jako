package kotlin

import java.lang.lockObjectMonitor
import java.lang.unlockObjectMonitor

actual inline fun <R> synchronized(lock: Any, block: () -> R): R {
    val monitor = lockObjectMonitor(lock)
    try {
        return block()
    } finally {
        unlockObjectMonitor(monitor)
    }
}

@PublishedApi
internal inline fun <R> jakoSynchronized(lock: Any, block: () -> R): R {
    val monitor = lockObjectMonitor(lock)
    try {
        return block()
    } finally {
        unlockObjectMonitor(monitor)
    }
}


