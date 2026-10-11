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

public fun thread(
    start: Boolean = true,
    isDaemon: Boolean = false,
    name: String? = null,
    priority: Int = -1,
    block: () -> Unit
): java.lang.Thread {
    val thread = object : java.lang.Thread() {
        override fun run() {
            block()
        }
    }
    if (name != null) thread.setName(name)
    if (isDaemon) thread.setDaemon(true)
    if (start) thread.start()
    return thread
}

