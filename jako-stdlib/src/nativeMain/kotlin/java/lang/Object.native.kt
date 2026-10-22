package java.lang

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.AtomicReference
import kotlin.native.identityHashCode

@PublishedApi
internal class ObjectMonitor {
    val lock = ReentrantLock()
    val cond: Condition = lock.newCondition()
}

private class IdentityKey(val obj: Any) {
    override fun hashCode(): Int = obj.identityHashCode()
    override fun equals(other: Any?): Boolean = other is IdentityKey && other.obj === obj
}

private val globalMonitorTable = AtomicReference<Map<IdentityKey, ObjectMonitor>>(emptyMap())

private val globalMonitorLock = ReentrantLock()

@PublishedApi
internal fun getObjectMonitor(obj: Any): ObjectMonitor {
    val key = IdentityKey(obj)
    globalMonitorTable.value[key]?.let { return it }

    globalMonitorLock.lock()
    try {
        val current = globalMonitorTable.value
        current[key]?.let { return it }
        val newMonitor = ObjectMonitor()
        globalMonitorTable.value = current + (key to newMonitor)
        return newMonitor
    } finally {
        globalMonitorLock.unlock()
    }
}

private val staticClassMonitorLockObj = Any()

@PublishedApi
internal fun lockObjectMonitor(obj: Any?): Any {
    val target = obj ?: staticClassMonitorLockObj
    val monitor = getObjectMonitor(target)
    monitor.lock.lock()
    return monitor
}

@PublishedApi
internal fun unlockObjectMonitor(monitor: Any) {
    (monitor as ObjectMonitor).lock.unlock()
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
