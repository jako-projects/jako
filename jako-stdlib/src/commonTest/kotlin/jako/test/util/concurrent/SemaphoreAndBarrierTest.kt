package jako.test.util.concurrent

import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Semaphore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SemaphoreAndBarrierTest {

    @Test
    fun testSemaphore() {
        val sem = Semaphore(3)
        assertEquals(3, sem.availablePermits())
        assertTrue(sem.tryAcquire())
        assertEquals(2, sem.availablePermits())
        assertTrue(sem.tryAcquire(2))
        assertEquals(0, sem.availablePermits())
        assertFalse(sem.tryAcquire())

        sem.release(3)
        assertEquals(3, sem.availablePermits())
        assertEquals(3, sem.drainPermits())
        assertEquals(0, sem.availablePermits())
    }

    @Test
    fun testCyclicBarrier() {
        var actionExecuted = false
        val barrier = CyclicBarrier(1) {
            actionExecuted = true
        }

        assertEquals(1, barrier.parties)
        assertEquals(0, barrier.numberWaiting)
        assertFalse(barrier.isBroken)

        val idx = barrier.await()
        assertEquals(0, idx)
        assertTrue(actionExecuted)
    }

    @Test
    fun testCopyOnWriteArraySet() {
        val set = CopyOnWriteArraySet<String>()
        assertTrue(set.add("A"))
        assertFalse(set.add("A"))
        assertTrue(set.add("B"))

        assertEquals(2, set.size)
        assertTrue(set.contains("A"))
        assertTrue(set.contains("B"))
        assertFalse(set.contains("C"))
    }
}
