package java.util.concurrent.locks

actual interface ReadWriteLock {
    actual fun readLock(): Lock
    actual fun writeLock(): Lock
}
