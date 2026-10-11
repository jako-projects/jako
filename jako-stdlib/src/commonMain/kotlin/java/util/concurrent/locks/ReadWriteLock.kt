@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.locks

expect interface ReadWriteLock {
    fun readLock(): Lock
    fun writeLock(): Lock
}
