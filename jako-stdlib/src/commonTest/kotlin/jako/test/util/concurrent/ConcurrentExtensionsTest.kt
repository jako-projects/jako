package jako.test.util.concurrent

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConcurrentExtensionsTest {

    @Test
    fun testLockWithLock() {
        val lock = ReentrantLock()
        var executed = false
        val result = lock.withLock {
            executed = true
            "Lock Result"
        }
        assertTrue(executed)
        assertEquals("Lock Result", result)
    }
}

