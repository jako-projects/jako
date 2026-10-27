package java.lang

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

@PublishedApi
internal class ObjectMonitor {
    val lock = ReentrantLock()
    val cond: Condition = lock.newCondition()
}

private class IdentityKey(val obj: Any) {
    override fun hashCode(): Int = obj.hashCode()
    override fun equals(other: Any?): Boolean = other is IdentityKey && other.obj === obj
}

private val globalMonitorTable = HashMap<IdentityKey, ObjectMonitor>()

@PublishedApi
internal fun getObjectMonitor(obj: Any): ObjectMonitor {
    val key = IdentityKey(obj)
    return globalMonitorTable.getOrPut(key) { ObjectMonitor() }
}

private val staticClassMonitorLockObj = Any()

@PublishedApi
internal actual fun lockObjectMonitor(obj: Any?): Any {
    val target = obj ?: staticClassMonitorLockObj
    val monitor = getObjectMonitor(target)
    monitor.lock.lock()
    return monitor
}

@PublishedApi
internal actual fun unlockObjectMonitor(monitor: Any) {
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
    if (timeoutMillis < 0L) throw IllegalArgumentException("timeout value is negative")
    if (nanos < 0 || nanos > 999999) throw IllegalArgumentException("nanosecond timeout value out of range")
    val monitor = getObjectMonitor(obj)
    if (!monitor.lock.isHeldByCurrentThread()) {
        throw IllegalMonitorStateException("Current thread does not hold monitor lock")
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
    if (!monitor.lock.isHeldByCurrentThread()) {
        throw IllegalMonitorStateException("Current thread does not hold monitor lock")
    }
    monitor.cond.signal()
}

private fun notifyAllObject(obj: Any) {
    val monitor = getObjectMonitor(obj)
    if (!monitor.lock.isHeldByCurrentThread()) {
        throw IllegalMonitorStateException("Current thread does not hold monitor lock")
    }
    monitor.cond.signalAll()
}
