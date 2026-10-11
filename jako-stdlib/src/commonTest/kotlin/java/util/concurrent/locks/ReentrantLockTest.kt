package net.bennyhuo.jako.stdlib.test

import java.util.concurrent.locks.ReentrantLock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReentrantLockTest {

    @Test
    fun testReentrantLockBasic() {
        val lock = ReentrantLock()
        lock.lock()
        try {
            assertTrue(lock.isHeldByCurrentThread())
            assertEquals(1, lock.getHoldCount())

            lock.lock()
            try {
                assertEquals(2, lock.getHoldCount())
            } finally {
                lock.unlock()
            }

            assertEquals(1, lock.getHoldCount())
        } finally {
            lock.unlock()
        }
        assertEquals(0, lock.getHoldCount())
    }
}
