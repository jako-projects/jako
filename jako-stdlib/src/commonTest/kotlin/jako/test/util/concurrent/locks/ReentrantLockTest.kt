package jako.test.util.concurrent.locks

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.test.*

class ReentrantLockTest {

    @Test
    fun testReentrantLockBasic() {
        val lock = ReentrantLock()

        lock.lock()
        try {
            assertTrue(lock.isHeldByCurrentThread())
        } finally {
            lock.unlock()
        }

        assertFalse(lock.isHeldByCurrentThread())
    }

    @Test
    fun testTryLock() {
        val lock = ReentrantLock()

        assertTrue(lock.tryLock())
        try {
            assertTrue(lock.isHeldByCurrentThread())
        } finally {
            lock.unlock()
        }

        assertTrue(lock.tryLock(100, TimeUnit.MILLISECONDS))
        try {
            assertTrue(lock.isHeldByCurrentThread())
        } finally {
            lock.unlock()
        }
    }
}
