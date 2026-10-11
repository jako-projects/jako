package jako.test.util.concurrent

import kotlin.concurrent.withLock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocksTest {

    @Test
    fun testReentrantLock() {
        val lock = ReentrantLock()
        assertFalse(lock.isLocked)
        lock.withLock {
            assertTrue(lock.isLocked)
        }
        assertFalse(lock.isLocked)
    }

    @Test
    fun testReentrantReadWriteLock() {
        val rwLock = ReentrantReadWriteLock()
        val readLock = rwLock.readLock()
        val writeLock = rwLock.writeLock()

        readLock.lock()
        assertEquals(1, rwLock.getReadLockCount())
        assertFalse(rwLock.isWriteLocked())
        readLock.unlock()
        assertEquals(0, rwLock.getReadLockCount())

        writeLock.lock()
        assertTrue(rwLock.isWriteLocked())
        writeLock.unlock()
        assertFalse(rwLock.isWriteLocked())
    }
}
