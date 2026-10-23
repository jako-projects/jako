package kotlin.concurrent

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock

public inline fun <T> Lock.withLock(action: () -> T): T {
    lock()
    try {
        return action()
    } finally {
        unlock()
    }
}

public inline fun <T> ReentrantReadWriteLock.read(action: () -> T): T =
    readLock().withLock(action)

public inline fun <T> ReentrantReadWriteLock.write(action: () -> T): T =
    writeLock().withLock(action)
