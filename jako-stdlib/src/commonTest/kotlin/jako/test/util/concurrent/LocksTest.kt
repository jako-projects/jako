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
        val rwLock = ReentrantReadWriteLock(true)
        assertTrue(rwLock.isFair())
        val readLock: ReentrantReadWriteLock.ReadLock = rwLock.readLock()
        val writeLock: ReentrantReadWriteLock.WriteLock = rwLock.writeLock()

        kotlin.test.assertFailsWith<UnsupportedOperationException> {
            readLock.newCondition()
        }
        val cond = writeLock.newCondition()
        kotlin.test.assertFailsWith<java.lang.IllegalMonitorStateException> {
            cond.signal()
        }

        readLock.lock()
        assertTrue(readLock.tryLock())
        assertEquals(2, rwLock.getReadLockCount())
        assertEquals(2, rwLock.getReadHoldCount())
        assertFalse(rwLock.isWriteLocked())
        readLock.unlock()
        readLock.unlock()
        assertEquals(0, rwLock.getReadLockCount())
        assertEquals(0, rwLock.getReadHoldCount())

        assertTrue(writeLock.tryLock(10L, java.util.concurrent.TimeUnit.MILLISECONDS))
        writeLock.lockInterruptibly()
        assertTrue(rwLock.isWriteLocked())
        assertTrue(rwLock.isWriteLockedByCurrentThread())
        assertTrue(writeLock.isHeldByCurrentThread())
        assertEquals(2, rwLock.getWriteHoldCount())
        assertEquals(2, writeLock.getHoldCount())

        // Verify WriteLock Condition timed await & signal while holding reentrant writeLock
        assertFalse(cond.await(5L, java.util.concurrent.TimeUnit.MILLISECONDS))
        assertEquals(2, writeLock.getHoldCount())
        cond.signal()
        cond.signalAll()

        // Lock downgrade: acquire readLock while holding writeLock
        assertTrue(readLock.tryLock(10L, java.util.concurrent.TimeUnit.MILLISECONDS))
        assertEquals(1, rwLock.getReadLockCount())

        writeLock.unlock()
        assertEquals(1, writeLock.getHoldCount())
        writeLock.unlock()
        assertFalse(rwLock.isWriteLocked())
        assertEquals(0, writeLock.getHoldCount())

        readLock.unlock()
        assertEquals(0, rwLock.getReadLockCount())
    }
}
