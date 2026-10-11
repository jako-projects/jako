package java.lang

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.AtomicReference

@PublishedApi
internal class ObjectMonitor {
    val lock = ReentrantLock()
    val cond: Condition = lock.newCondition()
}

private val globalMonitorTable = AtomicReference<Map<Any, ObjectMonitor>>(emptyMap())
private val globalMonitorLock = ReentrantLock()

@PublishedApi
internal fun getObjectMonitor(obj: Any): ObjectMonitor {
    globalMonitorTable.value[obj]?.let { return it }

    globalMonitorLock.lock()
    try {
        val current = globalMonitorTable.value
        current[obj]?.let { return it }
        val newMonitor = ObjectMonitor()
        globalMonitorTable.value = current + (obj to newMonitor)
        return newMonitor
    } finally {
        globalMonitorLock.unlock()
    }
}

actual open class Object {
    actual fun wait() {
        waitObject(this)
    }

    actual fun wait(timeoutMillis: Long) {
        waitObject(this, timeoutMillis, 0)
    }

    actual fun wait(timeoutMillis: Long, nanos: Int) {
        waitObject(this, timeoutMillis, nanos)
    }

    actual fun notify() {
        notifyObject(this)
    }

    actual fun notifyAll() {
        notifyAllObject(this)
    }
}

private fun waitObject(obj: Any, timeoutMillis: Long = 0L, nanos: Int = 0) {
    val monitor = getObjectMonitor(obj)
    check(monitor.lock.isHeldByCurrentThread()) {
        "IllegalMonitorStateException: Current thread does not hold monitor lock"
    }
    if (timeoutMillis > 0 || nanos > 0) {
        val totalNanos = timeoutMillis * 1_000_000L + nanos
        monitor.cond.awaitNanos(totalNanos)
    } else {
        monitor.cond.await()
    }
}

private fun notifyObject(obj: Any) {
    val monitor = getObjectMonitor(obj)
    check(monitor.lock.isHeldByCurrentThread()) {
        "IllegalMonitorStateException: Current thread does not hold monitor lock"
    }
    monitor.cond.signal()
}

private fun notifyAllObject(obj: Any) {
    val monitor = getObjectMonitor(obj)
    check(monitor.lock.isHeldByCurrentThread()) {
        "IllegalMonitorStateException: Current thread does not hold monitor lock"
    }
    monitor.cond.signalAll()
}
