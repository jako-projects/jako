package java.util.concurrent.locks

import java.util.concurrent.TimeUnit

actual interface Lock {
    actual fun lock()
    actual fun lockInterruptibly()
    actual fun tryLock(): Boolean
    actual fun tryLock(time: Long, unit: TimeUnit): Boolean
    actual fun unlock()
    actual fun newCondition(): Condition
}

public inline fun <T> Lock.withLock(action: () -> T): T {
    lock()
    try {
        return action()
    } finally {
        unlock()
    }
}
